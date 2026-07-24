# Pull request build-fix summary

This branch validates the Forge 1.20.1 fix for the Prah ash echo:

- the private `ArmorStand#setSmall(boolean)` call is not used;
- the echo relies on the constructor's default full-size armor stand;
- a regression guard checks the source before compilation;
- GitHub Actions runs `./gradlew clean build --no-daemon`.
