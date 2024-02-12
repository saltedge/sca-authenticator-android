#!/usr/bin/env bash
#  Copyright (c) 2019 Salt Edge Inc.

core_report_file="authenticator_core/build/reports/jacocoTestReport/html/index.html"
sdk_v1_report_file="authenticator_sdk_v1/build/reports/jacocoTestReport/html/index.html"
sdk_v2_report_file="authenticator_sdk_v2/build/reports/jacocoTestReport/html/index.html"
app_report_file="app/build/reports/jacocoTestReport/html/index.html"

./gradlew clean

./gradlew --stacktrace authenticator_core:jacocoTestReport 2>&1
status=$?
echo "authenticator_core test result status:$status"
[[ ${status} -ne 0 ]] && exit ${status}

./gradlew --stacktrace authenticator_sdk_v1:jacocoTestReport 2>&1
status=$?
echo "authenticator_sdk_v1 test result status:$status"
[[ ${status} -ne 0 ]] && exit ${status}

./gradlew --stacktrace authenticator_sdk_v2:jacocoTestReport 2>&1
status=$?
echo "authenticator_sdk_v2 test result status:$status"
[[ ${status} -ne 0 ]] && exit ${status}

./gradlew --stacktrace app:jacocoTestReport 2>&1
status=$?
echo "application test result status:$status"
[[ ${status} -ne 0 ]] && exit ${status}

core_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$core_report_file" | grep -Eo "\d{1,3}\%")
sdk_v1_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$sdk_v1_report_file" | grep -Eo "\d{1,3}\%")
sdk_v2_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$sdk_v2_report_file" | grep -Eo "\d{1,3}\%")
app_coverage=$(grep -Eo "Total.+?(\d{1,3}\%)" "$app_report_file" | grep -Eo "\d{1,3}\%")

echo "Total Coverage: Core:$core_coverage, SDK v1:$sdk_v1_coverage, SDK v2:$sdk_v2_coverage, APP:$app_coverage"
