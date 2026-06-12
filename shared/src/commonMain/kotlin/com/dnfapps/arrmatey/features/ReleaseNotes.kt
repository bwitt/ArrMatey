package com.dnfapps.arrmatey.features

import com.dnfapps.arrmatey.shared.MR

object ReleaseNotes {

    val updates = listOf(
        FeatureUpdate(
            buildCode = 1,
            title = MR.strings.v1_title,
            androidContentFile = MR.files.release_0_0_4_txt
        ),
        FeatureUpdate(
            buildCode = 2,
            title = MR.strings.v2_title,
            androidContentFile = MR.files.release_0_1_0_txt
        ),
        FeatureUpdate(
            buildCode = 3,
            title = MR.strings.v3_title,
            androidContentFile = MR.files.release_0_3_0_txt
        ),
        FeatureUpdate(
            buildCode = 4,
            title = MR.strings.v4_title,
            androidContentFile = MR.files.release_0_4_0_txt,
            iosContentFile = MR.files.release_0_4_0_ios_txt
        ),
        FeatureUpdate(
            buildCode = 5,
            title = MR.strings.v5_title,
            androidContentFile = MR.files.release_0_4_2_txt
        ),
        FeatureUpdate(
            buildCode = 6,
            title = MR.strings.v6_title,
            androidContentFile = MR.files.release_0_5_0_txt
        )
    )

    val latestUpdate = updates.maxBy { it.buildCode }

}