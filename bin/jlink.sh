#!/bin/sh

./mvnw -DskipTests=true clean verify jlink:jlink
