FROM maven:3.5.0-alpine

RUN apk update

RUN apk add git

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 

CMD git clone https://github.com/EduJGURJC/springbootdemotest.git; cd springbootdemotest; mvn clean test;
