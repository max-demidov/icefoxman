git clone https://github.com/max-demidov/icefoxman.git

cd icefoxman

mvn compile install

mvn -q exec:java nz.wex.icefoxman.trading.runner.Runner -DauthDataDir=<your dir that has "key.txt" and "secret.txt" files>
