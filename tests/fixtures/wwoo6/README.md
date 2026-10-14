These four cloud source files are test-only copies from WWOO backport commit d8be782f4f856522bcbcf71fe1e21d2cf486c543, Actions run 38337095698 (build .6).

Their Java package names are relocated into a neutral test namespace; the reference cloud logic is unchanged. They are compiled into an isolated legacy-WWOO fixture to exercise both mods' actual client mixins together. They are excluded from the installable cloud JAR. No world-generation or other WWOO classes are included.
