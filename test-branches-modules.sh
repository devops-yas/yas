#!/usr/bin/env bash
set -u

export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

MODULES=(
  "test/customer-unit-test:customer:mvn -U clean test jacoco:report -pl customer -am"
  "test/order-unit-test:order:mvn -U clean test jacoco:report -pl order -am"
  "test/storefront-bff-unit-test:storefront-bff:mvn -U clean test jacoco:report -pl storefront-bff -am"
  "test/payment-paypal-unit-test:payment-paypal:mvn -U clean test jacoco:report -pl payment-paypal -am"
  "test/recommendation-unit-test:recommendation:mvn -U clean jacoco:prepare-agent test jacoco:report -pl recommendation -am"
  "test/sampledata-unit-test:sampledata:mvn -U clean test jacoco:report -pl sampledata -am"
  "test/search-unit-test:search:mvn -U clean jacoco:prepare-agent test jacoco:report -pl search -am"
)

echo "Java:"
java -version
echo

for item in "${MODULES[@]}"; do
  IFS=':' read -r branch module cmd <<< "$item"

  echo "============================================================"
  echo "Branch : $branch"
  echo "Module : $module"
  echo "Command: $cmd"
  echo "============================================================"

  git checkout "$branch"
  if [ $? -ne 0 ]; then
    echo "[FAIL] Cannot checkout branch: $branch"
    echo
    continue
  fi

  eval "$cmd"
  status=$?

  if [ $status -eq 0 ]; then
    echo "[PASS] Build/test passed for $module"
  else
    echo "[FAIL] Build/test failed for $module with exit code $status"
  fi

  csv="$module/target/site/jacoco/jacoco.csv"
  if [ -f "$csv" ]; then
    echo "Coverage summary:"
    awk -F, 'NR>1 {missed+=$8; covered+=$9} END {
      total=missed+covered;
      if (total > 0) {
        printf "Total line coverage: %.2f%% missed=%d covered=%d\n", (covered/total)*100, missed, covered
      } else {
        print "No line coverage data"
      }
    }' "$csv"

    echo "Lowest coverage classes:"
    awk -F, 'NR>1 {
      total=$8+$9;
      if(total>0) printf "%-55s %6.2f%% missed=%s covered=%s\n", $3, ($9/total)*100, $8, $9
    }' "$csv" | sort -k2 -n | head -10
  else
    echo "Coverage CSV not found: $csv"
  fi

  echo
done

echo "============================================================"
echo "Done testing all configured branches/modules"
echo "============================================================"
