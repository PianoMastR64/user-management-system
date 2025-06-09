
Start the app with `-Dspring-boot.run.profiles=dev` so the `JWT_SECRET` environment variable can be
injected as a property into `JwtUtil` via the `application-dev.properties` `jwt.secret`.