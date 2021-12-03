# my implement didn't refer to starting codes, so there aresome tips for test

## all commands should run in "snake" directory
## `cd snake`
## `bazel run :snake_app -- start_server`
## `bazel run :snake_app -- create 123 snakemaster 10.0.0.1 8181`
## `bazel run :snake_app -- join 123 masterkiller 10.0.0.2 8585`

## if you want to change snake run speed, modify GAME_SPEED_MS at Config.java

## if you want to get ride of crack window rule, remove condition isCollideWindow() at line 126 and line 155 in GameState.java
