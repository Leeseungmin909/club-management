-- 동아리 조아 DB 스키마
-- 테이블 정의는 dbdiagram.io 설계(MySQL 내보내기)와 동일하게 유지한다.
-- 실행: sudo mysql < schema.sql   (기존 clubapp DB를 지우고 새로 만든다)

DROP DATABASE IF EXISTS clubapp;
CREATE DATABASE clubapp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clubapp;

-- 테이블 (dbdiagram.io 내보내기) -------------------------------------------

CREATE TABLE `club` (
  `id` int PRIMARY KEY COMMENT '항상 1 (동아리는 하나)',
  `name` varchar(50) NOT NULL COMMENT '동아리 이름',
  `school` varchar(50) COMMENT '학교',
  `intro` varchar(500) COMMENT '소개',
  `image_path` varchar(255) COMMENT '대표(프로필) 이미지 경로',
  `banner_path` varchar(255) COMMENT '배경(커버) 이미지 경로'
);

CREATE TABLE `department` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '학과 번호',
  `name` varchar(50) UNIQUE NOT NULL COMMENT '학과 이름 (검색 대상)'
);

CREATE TABLE `member` (
  `student_no` varchar(20) PRIMARY KEY COMMENT '학번 (숫자 8자리)',
  `naver_id` varchar(100) UNIQUE COMMENT '네이버 고유 ID (네이버로 가입한 회원, 로그인할 때 회원 찾기용)',
  `kakao_id` varchar(100) UNIQUE COMMENT '카카오 고유 ID (카카오로 가입한 회원, 로그인할 때 회원 찾기용)',
  `name` varchar(30) NOT NULL COMMENT '이름 (네이버 또는 가입 시 입력)',
  `department_id` int NOT NULL COMMENT '학과 번호 (가입 시 검색 후 선택)',
  `phone` varchar(20) COMMENT '전화번호 (네이버 또는 가입 시 입력), 탈퇴 시 NULL',
  `profile_image` varchar(255) COMMENT '프로필 이미지 경로',
  `role` ENUM ('ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
  `status` ENUM ('PENDING', 'ACTIVE', 'WITHDRAWN', 'KICKED') NOT NULL DEFAULT 'PENDING',
  `requested_at` datetime NOT NULL COMMENT '가입 신청일',
  `approved_at` datetime COMMENT '승인일 (그래프 신규 가입자 기준)',
  `left_at` datetime COMMENT '탈퇴·추방일 (그래프 탈퇴자 기준)'
);

CREATE TABLE `notice` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '공지 번호',
  `writer_no` varchar(20) NOT NULL COMMENT '작성자 학번',
  `title` varchar(100) NOT NULL COMMENT '제목 (검색 대상)',
  `content` text NOT NULL COMMENT '본문',
  `is_pinned` boolean NOT NULL DEFAULT false COMMENT '상단 고정 여부',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '조회수',
  `created_at` datetime NOT NULL,
  `updated_at` datetime
);

CREATE TABLE `schedule` (
  `id` int PRIMARY KEY AUTO_INCREMENT COMMENT '일정 번호',
  `writer_no` varchar(20) NOT NULL COMMENT '등록자 학번',
  `title` varchar(100) NOT NULL,
  `content` text,
  `start_at` datetime NOT NULL COMMENT '시작 시간',
  `end_at` datetime COMMENT '종료 시간',
  `place_name` varchar(100) COMMENT '장소 이름',
  `address` varchar(200) COMMENT '주소',
  `latitude` decimal(10,7) COMMENT '위도',
  `longitude` decimal(10,7) COMMENT '경도',
  `created_at` datetime NOT NULL,
  `updated_at` datetime COMMENT '수정일'
);

CREATE TABLE `attendance` (
  `schedule_id` int NOT NULL,
  `student_no` varchar(20) NOT NULL,
  `status` ENUM ('ATTEND', 'ABSENT') NOT NULL,
  `updated_at` datetime NOT NULL COMMENT '마지막으로 누른 시간',
  PRIMARY KEY (`schedule_id`, `student_no`)
);

CREATE TABLE `chat_message` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `sender_no` varchar(20) NOT NULL COMMENT '보낸 사람 학번',
  `message_type` ENUM ('TEXT', 'IMAGE', 'VIDEO') NOT NULL DEFAULT 'TEXT',
  `content` text COMMENT '글자 내용 (파일 메시지는 비어 있음)',
  `file_path` varchar(255) COMMENT '이미지·동영상 저장 경로',
  `file_name` varchar(255) COMMENT '원래 파일 이름',
  `created_at` datetime NOT NULL COMMENT '보낸 시간',
  `updated_at` datetime COMMENT '수정 시간 (수정됨 표시)'
);

CREATE TABLE `notification` (
  `id` int PRIMARY KEY AUTO_INCREMENT,
  `receiver_no` varchar(20) NOT NULL COMMENT '받는 사람 학번',
  `type` ENUM ('APPROVED', 'NOTICE', 'SCHEDULE') NOT NULL,
  `message` varchar(200) NOT NULL COMMENT '알림 문구',
  `link_url` varchar(200) COMMENT '누르면 이동할 주소',
  `is_read` boolean NOT NULL DEFAULT false,
  `created_at` datetime NOT NULL
);

ALTER TABLE `club` COMMENT = '동아리 정보. 한 줄만 존재 (SFR-05)';

ALTER TABLE `department` COMMENT = '학과 목록. 가입 시 검색해서 선택 (SFR-02)';

ALTER TABLE `member` COMMENT = '회원, 역할, 가입 상태 (SFR-01~12, 26)';

ALTER TABLE `notice` COMMENT = '공지사항 (SFR-13~16, 30)';

ALTER TABLE `schedule` COMMENT = '일정과 장소 좌표 (SFR-17~19, 23, 24)';

ALTER TABLE `attendance` COMMENT = '일정별 참석·불참. 줄이 없으면 미응답 (SFR-20~22, 26)';

ALTER TABLE `chat_message` COMMENT = '동아리 단체 채팅, 이미지·동영상 포함 (SFR-29)';

ALTER TABLE `notification` COMMENT = '알림 (SFR-27, 28)';

ALTER TABLE `member` ADD FOREIGN KEY (`department_id`) REFERENCES `department` (`id`);

ALTER TABLE `notice` ADD FOREIGN KEY (`writer_no`) REFERENCES `member` (`student_no`) ON UPDATE CASCADE;

ALTER TABLE `schedule` ADD FOREIGN KEY (`writer_no`) REFERENCES `member` (`student_no`) ON UPDATE CASCADE;

ALTER TABLE `attendance` ADD FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE CASCADE;

ALTER TABLE `attendance` ADD FOREIGN KEY (`student_no`) REFERENCES `member` (`student_no`) ON UPDATE CASCADE;

ALTER TABLE `chat_message` ADD FOREIGN KEY (`sender_no`) REFERENCES `member` (`student_no`) ON UPDATE CASCADE;

ALTER TABLE `notification` ADD FOREIGN KEY (`receiver_no`) REFERENCES `member` (`student_no`) ON UPDATE CASCADE;

-- 초기 데이터 ------------------------------------------------------------

INSERT INTO club (id, name, school, intro) VALUES
    (1, 'CPU', '동의대학교', '동아리 소개를 입력하세요.');

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
