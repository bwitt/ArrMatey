import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @StateObject private var queueViewModel = ActivityQueueViewModelS()
    @StateObject private var preferences = PreferencesViewModel()
    @State private var hasInitializedSelection = false

    init() {
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()

        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    var body: some View {
        Group {
            if preferences.isFirstLaunch {
                OnboardingView {
                    preferences.markFirstLaunchComplete()
                }
            } else if preferences.bottomTabItems.isEmpty {
                ProgressView()
            } else if horizontalSizeClass == .regular {
                SidebarSplitViewContainer(
                    navigationManager: navigationManager,
                    preferences: preferences
                )
            } else {
                CompactTabContainer(
                    navigationManager: navigationManager,
                    preferences: preferences
                )
            }
        }
        .onAppear {
            validateSelection(items: preferences.bottomTabItems)
            preferences.trimBottomTabs(to: NavigationManager.compactTabLimit)
        }
        .onChange(of: preferences.bottomTabItems.map { $0.key }) { _, _ in
            validateSelection(items: preferences.bottomTabItems)
            preferences.trimBottomTabs(to: NavigationManager.compactTabLimit)
        }
    }

    private func validateSelection(items: [AnyTabItem]) {
        guard let firstTab = items.first else { return }

        if !hasInitializedSelection {
            hasInitializedSelection = true
            navigationManager.selectedTab = firstTab
            return
        }

        if !items.contains(where: { $0.key == navigationManager.selectedTab.key }) {
            navigationManager.selectedTab = firstTab
        }
    }
}

// MARK: - iPhone Compact Tab Container

struct CompactTabContainer: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel

    var barTabs: [AnyTabItem] { Array(preferences.bottomTabItems.prefix(NavigationManager.compactTabLimit)) }
    var moreTabs: [AnyTabItem] { Array(preferences.bottomTabItems.dropFirst(NavigationManager.compactTabLimit)) + preferences.drawerTabs }

    var body: some View {
        TabView(selection: Binding(
            get: { navigationManager.showLauncher ? "launcher" : navigationManager.selectedTab.key },
            set: { navigationManager.selectTab(key: $0) }
        )) {
            ForEach(barTabs, id: \.key) { tabItem in
                NavigationStack(path: navigationManager.pathBinding(for: tabItem.key)) {
                    TabItemContent(tabItem: tabItem.item)
                        .withAppDestinations()
                }
                .tabItem {
                    TabLabel(item: tabItem.item, useServiceLogos: preferences.useServiceNavLogos)
                }
                .tag(tabItem.key)
            }

            NavigationStack(path: navigationManager.pathBinding(for: "launcher")) {
                MoreDrawerView(navigationManager: navigationManager, preferences: preferences, tabs: moreTabs)
                    .withAppDestinations()
            }
            .tabItem {
                Label(MR.strings().navigation_items_drawer.localized(), systemImage: "ellipsis.circle.fill")
            }
            .tag("launcher")
        }
        .onAppear {
            navigationManager.usesSidebar = false
            ensureSelectionInBar()
        }
        .onChange(of: barTabs.map { $0.key }) { _, _ in
            ensureSelectionInBar()
        }
    }

    private func ensureSelectionInBar() {
        guard !barTabs.contains(where: { $0.key == navigationManager.selectedTab.key }),
              let first = barTabs.first else { return }
        navigationManager.selectedTab = first
    }
}

// MARK: - iPad Sidebar / Split View Container

struct SidebarSplitViewContainer: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel
    @State private var columnVisibility: NavigationSplitViewVisibility = .all

    var body: some View {
        NavigationSplitView(columnVisibility: $columnVisibility) {
            SidebarContentView(
                navigationManager: navigationManager,
                preferences: preferences
            )
            .navigationTitle("ArrMatey")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        navigationManager.push(SettingsRoute.navigationConfig)
                    } label: {
                        Image(systemName: "slider.horizontal.3")
                    }
                }
            }
        } detail: {
            let contextKey = navigationManager.showLauncher ? "launcher" : navigationManager.selectedTab.key
            NavigationStack(path: navigationManager.pathBinding(for: contextKey)) {
                Group {
                    if navigationManager.showLauncher {
                        MoreDrawerView(navigationManager: navigationManager, preferences: preferences, tabs: preferences.drawerTabs)
                    } else {
                        TabItemContent(tabItem: navigationManager.selectedTab.item)
                    }
                }
                .withAppDestinations()
            }
            .id(contextKey)
        }
        .navigationSplitViewStyle(.balanced)
        .onAppear {
            navigationManager.usesSidebar = true
        }
    }
}

struct SidebarContentView: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel

    private var allActiveTabs: [AnyTabItem] {
        preferences.bottomTabItems + preferences.drawerTabs
    }

    private var coreTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .dashboard || standard == .library
        }
    }

    private var mediaTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .shows || standard == .movies || standard == .music || standard == .books || standard == .audiobooks
        }
    }

    private var activityTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .activity || standard == .calendar || standard == .downloads || standard == .requests || standard == .discover
        }
    }

    private var managementTabs: [AnyTabItem] {
        allActiveTabs.filter { tab in
            guard let standard = tab.item as? TabItemStandard else { return false }
            return standard == .prowlarr || standard == .bazarr || standard == .tracearr
        }
    }

    private var customWebpages: [AnyTabItem] {
        allActiveTabs.filter { $0.item is TabItemCustomWebpage }
    }

    var body: some View {
        List(selection: Binding(
            get: { navigationManager.showLauncher ? "launcher" : navigationManager.selectedTab.key },
            set: { navigationManager.selectTab(key: $0) }
        )) {
            if !coreTabs.isEmpty {
                Section {
                    ForEach(coreTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !mediaTabs.isEmpty {
                Section(header: Text("Media")) {
                    ForEach(mediaTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !activityTabs.isEmpty {
                Section(header: Text("Activity")) {
                    ForEach(activityTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !managementTabs.isEmpty {
                Section(header: Text("Management")) {
                    ForEach(managementTabs, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            if !customWebpages.isEmpty {
                Section(header: Text("Web Pages")) {
                    ForEach(customWebpages, id: \.key) { tab in
                        sidebarRow(for: tab)
                    }
                }
            }

            Section {
                Button {
                    navigationManager.push(SettingsRoute.services)
                } label: {
                    Label(MR.strings().settings.localized(), systemImage: "gearshape")
                        .foregroundStyle(.primary)
                }
            }
        }
        .listStyle(.sidebar)
    }

    @ViewBuilder
    private func sidebarRow(for tab: AnyTabItem) -> some View {
        HStack {
            TabLabel(item: tab.item, useServiceLogos: preferences.useServiceNavLogos)
            Spacer()
        }
        .tag(tab.key)
    }
}

// MARK: - More / Drawer View

struct MoreDrawerView: View {
    @ObservedObject var navigationManager: NavigationManager
    @ObservedObject var preferences: PreferencesViewModel
    let tabs: [AnyTabItem]

    private let columns = [GridItem(.adaptive(minimum: 88), spacing: 20)]

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Drawer Grid
                LazyVGrid(columns: columns, spacing: 20) {
                    ForEach(tabs, id: \.key) { item in
                        NavigationLink(value: item) {
                            VStack(spacing: 8) {
                                launcherIcon(for: item.item)

                                Text(tabName(for: item.item))
                                    .font(.caption.weight(.medium))
                                    .lineLimit(1)
                                    .foregroundStyle(.primary)
                            }
                            .frame(width: 88, height: 88)
                            .background(Color(.secondarySystemGroupedBackground))
                            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 18, style: .continuous)
                                    .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
                            )
                            .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)

                // Quick Navigation / Settings Section
                VStack(spacing: 0) {
                    NavigationLink(value: SettingsRoute.navigationConfig) {
                        HStack {
                            Label(MR.strings().customize_navigation.localized(), systemImage: "slider.horizontal.3")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        }
                        .padding()
                    }

                    Divider()

                    NavigationLink(value: SettingsRoute.services) {
                        HStack {
                            Label(MR.strings().settings.localized(), systemImage: "gearshape.fill")
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption.bold())
                                .foregroundColor(.secondary)
                        }
                        .padding()
                    }
                }
                .background(Color(.secondarySystemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .padding(.horizontal, 20)
            }
        }
        .navigationTitle(MR.strings().navigation_items_drawer.localized())
        .navigationBarTitleDisplayMode(.large)
    }

    @ViewBuilder
    private func launcherIcon(for item: TabItem) -> some View {
        if preferences.useServiceNavLogos, let logo = item.associatedType?.tabIcon {
            logo.toImage(renderingMode: .template)
                .foregroundStyle(Color.accentColor)
        } else {
            Image(systemName: item.iosIcon)
                .font(.title2)
                .foregroundStyle(Color.accentColor)
        }
    }

    private func tabName(for item: TabItem) -> String {
        if let custom = item as? TabItemCustomWebpage {
            return custom.name
        }
        return item.resource.localized()
    }
}

// MARK: - Tab Label Component

struct TabLabel: View {
    let item: TabItem
    let useServiceLogos: Bool

    var body: some View {
        if let standard = item as? TabItemStandard {
            if useServiceLogos, let logo = standard.associatedType?.tabIcon {
                Label(
                    title: { Text(standard.resource.localized()) },
                    icon: { logo.toImage(renderingMode: .template) }
                )
            } else {
                Label(standard.resource.localized(), systemImage: standard.iosIcon)
            }
        } else if let custom = item as? TabItemCustomWebpage {
            Label(custom.name, systemImage: custom.iosIcon)
        }
    }
}
