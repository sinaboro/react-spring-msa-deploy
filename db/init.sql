-- init.sql - 데이터베이스 2개 만들기 (컨테이너가 처음 켜질 때 한 번만 자동 실행)
--
-- [MSA 개념] 서비스마다 자기 데이터베이스를 가진다 (Database per Service)
--   userdb : 회원 서비스(user-service) 전용  → users 테이블
--   postdb : 게시글 서비스(post-service) 전용 → posts 테이블
-- 테이블은 여기서 만들지 않는다. 각 서비스의 JPA(ddl-auto: update)가 Entity 를 보고 만든다.

CREATE DATABASE userdb;
CREATE DATABASE postdb;
