#  This file is part of the Salt Edge Authenticator distribution
#  (https:github.com/saltedge/sca-authenticator-android)
#  Copyright (c) 2019 Salt Edge Inc.
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, version 3.
#
#  This program is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program. If not, see <http:www.gnu.org/licenses/>.
#
#  For the additional permissions granted for Salt Edge Authenticator
#  under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md

#!/usr/bin/env bash

sdk_report_file="authenticator_sdk/build/reports/jacocoTestReport/html/index.html"
app_report_file="app/build/reports/jacocoTestReport/html/index.html"

adb uninstall com.saltedge.authenticator.tests 2>&1
adb uninstall com.saltedge.authenticator 2>&1

./gradlew clean
./gradlew --stacktrace authenticator_sdk:jacocoTestReport 2>&1
status=$?
echo "authenticator_sdk test:$status"
[[ ${status} -ne 0 ]] && exit ${status}
./gradlew --stacktrace app:jacocoTestReport 2>&1
status=$?
echo "app test:$status"
[[ ${status} -ne 0 ]] && exit ${status}

sdk_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$sdk_report_file" | grep -Eo "\d{1,3}\%")
app_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$app_report_file" | grep -Eo "\d{1,3}\%")

echo "Total Coverage: SDK:$sdk_coverage, APP:$app_coverage"