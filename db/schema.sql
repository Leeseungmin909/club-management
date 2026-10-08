-- 동아리 조아 DB 스키마 (계획서_변경사항.md 부록 기준)
-- 실행: sudo mysql < schema.sql   (기존 clubapp DB를 지우고 새로 만든다)

DROP DATABASE IF EXISTS clubapp;
CREATE DATABASE clubapp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clubapp;

CREATE TABLE club (
    id         INT          PRIMARY KEY,               -- 항상 1
    name       VARCHAR(50)  NOT NULL,
    school     VARCHAR(50),
    intro      VARCHAR(500),
    image_path VARCHAR(255)
);

CREATE TABLE department (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE member (
    student_no    VARCHAR(20)  PRIMARY KEY,             -- 학번 (숫자 8자리)
    naver_id      VARCHAR(100) NOT NULL UNIQUE,
    name          VARCHAR(30)  NOT NULL,
    department_id INT          NOT NULL,
    phone         VARCHAR(20),                          -- 탈퇴 시 NULL
    profile_image VARCHAR(255),
    role          ENUM('ADMIN','MEMBER')                      NOT NULL DEFAULT 'MEMBER',
    status        ENUM('PENDING','ACTIVE','WITHDRAWN','KICKED') NOT NULL DEFAULT 'PENDING',
    requested_at  DATETIME NOT NULL,
    approved_at   DATETIME,
    left_at       DATETIME,                             -- 탈퇴·추방일
    FOREIGN KEY (department_id) REFERENCES department (id)
);

CREATE TABLE notice (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    writer_no  VARCHAR(20)  NOT NULL,
    title      VARCHAR(100) NOT NULL,
    content    TEXT         NOT NULL,
    is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE,
    view_count INT          NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (writer_no) REFERENCES member (student_no) ON UPDATE CASCADE
);

CREATE TABLE schedule (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    writer_no  VARCHAR(20)   NOT NULL,
    title      VARCHAR(100)  NOT NULL,
    content    TEXT,
    start_at   DATETIME      NOT NULL,
    end_at     DATETIME,
    place_name VARCHAR(100),
    address    VARCHAR(200),
    latitude   DECIMAL(10,7),
    longitude  DECIMAL(10,7),
    created_at DATETIME      NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (writer_no) REFERENCES member (student_no) ON UPDATE CASCADE
);

CREATE TABLE attendance (
    schedule_id INT         NOT NULL,
    student_no  VARCHAR(20) NOT NULL,
    status      ENUM('ATTEND','ABSENT') NOT NULL,
    updated_at  DATETIME    NOT NULL,
    PRIMARY KEY (schedule_id, student_no),
    FOREIGN KEY (schedule_id) REFERENCES schedule (id) ON DELETE CASCADE,
    FOREIGN KEY (student_no)  REFERENCES member (student_no) ON UPDATE CASCADE
);

CREATE TABLE chat_message (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    sender_no    VARCHAR(20) NOT NULL,
    message_type ENUM('TEXT','IMAGE','VIDEO') NOT NULL DEFAULT 'TEXT',
    content      TEXT,                                  -- 파일 메시지는 비어 있음
    file_path    VARCHAR(255),
    file_name    VARCHAR(255),
    created_at   DATETIME NOT NULL,
    FOREIGN KEY (sender_no) REFERENCES member (student_no) ON UPDATE CASCADE
);

CREATE TABLE notification (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    receiver_no VARCHAR(20)  NOT NULL,
    type        ENUM('APPROVED','NOTICE','SCHEDULE') NOT NULL,
    message     VARCHAR(200) NOT NULL,
    link_url    VARCHAR(200),
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  DATETIME     NOT NULL,
    FOREIGN KEY (receiver_no) REFERENCES member (student_no) ON UPDATE CASCADE,
    INDEX (receiver_no, is_read)
);

-- 초기 데이터 ------------------------------------------------------------

INSERT INTO club (id, name, school, intro) VALUES
    (1, '동아리 조아', '동의대학교', '동아리 소개를 입력하세요.');

-- 학과 목록 (초안: 실제 동의대 학과 목록으로 교체·보완 필요)
INSERT INTO department (name) VALUES
    ('컴퓨터소프트웨어공학과'), ('컴퓨터공학과'), ('소프트웨어공학과'), ('인공지능학과'),
    ('게임공학과'), ('전자공학과'), ('정보통신공학과'), ('기계공학과'), ('건축학과'),
    ('경영학과'), ('회계학과'), ('국제무역학과'), ('경제학과'), ('행정학과'), ('법학과'),
    ('국어국문학과'), ('영어영문학과'), ('중국어학과'), ('일본어학과'),
    ('신문방송학과'), ('광고홍보학과'), ('문헌정보학과'), ('심리학과'), ('사회복지학과'),
    ('유아교육과'), ('간호학과'), ('임상병리학과'), ('물리치료학과'), ('식품영양학과'),
    ('한의예과'), ('디자인조형학과'), ('체육학과'), ('음악학과'), ('영화학과');

-- 최초 관리자: 첫 로그인·가입 신청 후 아래를 학번만 바꿔 실행
-- UPDATE member SET role = 'ADMIN', status = 'ACTIVE', approved_at = NOW() WHERE student_no = '20203188';
