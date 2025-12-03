/*
worket 더미데이터

<넣어야 할 것>
freelancer -> 거래상세 페이지에서 보일 더미테이터,
client -> 통합거래 페이지에서 에스크로 결제 할 계야갸 데이터

user

contract

transaction
*/

--1. user
-- freelancer 유저 등록 (더미 contract 만들 용도)
INSERT INTO `user` (`user_id`, `provider`, `oauth_id`, `name`, `role`, `status`, `phone`, `created_at`, `updated_at`, `passcode`)
VALUES
  (101, 'KAKAO', 'test_oauth_2', '유정호', 'FREELANCER', 'ACTIVE', '010-1111-1111', NOW(), NOW(), NULL),
  (102, 'KAKAO', 'test_oauth_2', '유승한', 'FREELANCER', 'ACTIVE', '010-2222-2222', NOW(), NOW(), NULL),
  (103, 'KAKAO', 'test_oauth_2', '김하영', 'FREELANCER', 'ACTIVE', '010-3333-3333', NOW(), NOW(), NULL),
  (104, 'KAKAO', 'test_oauth_2', '이시현', 'FREELANCER', 'ACTIVE', '010-1111-1111', NOW(), NOW(), NULL),
  (105, 'KAKAO', 'test_oauth_2', '심미경', 'FREELANCER', 'ACTIVE', '010-2222-2222', NOW(), NOW(), NULL),
  (106, 'KAKAO', 'test_oauth_2', '백인호', 'FREELANCER', 'ACTIVE', '010-1111-1111', NOW(), NOW(), NULL),
  (107, 'KAKAO', 'test_oauth_2', '공윤호', 'FREELANCER', 'ACTIVE', '010-2222-2222', NOW(), NOW(), NULL),
  (108, 'KAKAO', 'test_oauth_2', '강유민', 'FREELANCER', 'ACTIVE', '010-3333-3333', NOW(), NOW(), NULL);

-- 2. Freelancer Profile
INSERT INTO `freelancer_profile` (`freelancer_profile_id`, `user_id`, `birth_date`, `gender`, `business_sector`, `business_sector_years`, `business_registration_number`)
VALUES
  (101, 101, '1992-01-01', 'MALE', '웹/앱 개발자', 8, '1234567890'),
  (102, 102, '1998-01-01', 'MALE', 'MC/사회자', 3, '1234567890'),
  (103, 103, '2002-01-01', 'FEMALE', '작가/에디터', 2, '1234567890'),
  (104, 104, '1992-01-01', 'FEMALE', '웹/앱 개발자', 5, '1234567890'),
  (105, 105, '1998-01-01', 'FEMALE', 'MC/사회자', 3, '1234567890'),
  (106, 106, '2002-01-01', 'MALE', '작가/에디터', 2, '1234567890'),
  (107, 107, '1992-01-01', 'MALE', '웹/앱 개발자', 2, '1234567890'),
  (108, 108, '1998-01-01', 'FEMALE', '디자이너', 1, '1234567890');

-- 2️⃣ Contract

-- 통합거래페이지를 위한 더미데이터 (클라이언트 1명을 위한)
INSERT INTO `contract` (
  `contract_id`,
  `freelancer_id`,
  `client_id`,
  `type`,
  `title`,
  `amount`,
  `client_name`,
  `client_phone`,
  `freelancer_sign`,
  `client_sign`,
  `start_date`,
  `end_date`,
  `created_at`
) VALUES
  (101, 101, NULL, 'UPLOAD', '반응형 웹사이트 개발 프로젝트 계약', 3500000,
   '신수연', '010-1111-1111','','', '2025-12-01', '2026-01-03', NOW()),
  (102, 102, NULL, 'UPLOAD', '연말 기업 행사 진행 (MC) 용역 계약', 1500000,
   '신수연', '010-1111-1111','','', '2025-12-01', '2025-12-01', NOW()),
  (103, 103, NULL, 'UPLOAD', '블로그 마케팅 콘텐츠 기획 및 작성 계약', 1200000,
   '신수연', '010-1111-1111','','', '2025-12-01', '2026-01-20', NOW()),
  (104, 104, NULL, 'UPLOAD', '모바일 앱 기능 개선 및 유지보수 계약', 2500000,
   '신수연', '010-1111-1111','','', '2025-12-01', '2026-01-13', NOW()),
  (105, 105, NULL, 'UPLOAD', '지역 축제 개막식 MC 대행 계약', 650000,
   '신수연', '010-1111-1111','','', '2025-12-01', '2025-12-01', NOW()),
  (106, 106, NULL, 'CREATED', '도서 출판 교정 및 편집 계약', 1500000,
   '신수연', '010-1111-1111','https://s3-worket-bucket.s3.ap-northeast-2.amazonaws.com/signatures/1/FREELANCER-signature-2025-12-03T05%3A54%3A43.266Z.png','', '2025-10-01', '2025-11-30', NOW());

-- 3️⃣ Transaction

-- 3️⃣ Transaction (Contract ID 101~106에 연결되는 더미 데이터)
INSERT INTO `transaction` (
  `transaction_id`,
  `contract_id`,
  `status`,
  `settlement_amount`,
  `amount`,
  `client_bank`,
  `client_account`,
  `freelancer_bank`,
  `freelancer_account`,
  `signed_at`,
  `deposit_hold_at`,
  `payment_confirmed_at`,
  `settled_at`,
  `escrow_confim_tid`,
  `settlement_tid`,
  `created_at`,
  `updated_at`
)
VALUES
-- 1. 서명 완료 (SIGNED) - Contract 101 (3,500,000)
(1001, 101, 'SIGNED', NULL, 3500000.00, NULL, NULL, '020', '110-123-456789',
 DATE_SUB(NOW(), INTERVAL 15 DAY), NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),

-- 2. 서명 완료 (SIGNED) - Contract 102 (1,500,000)
(1002, 102, 'SIGNED', NULL, 1500000.00, NULL, NULL, '020', '001-02-0000-001',
 DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),

-- 3. 서명 완료 (SIGNED) - Contract 103 (1,200,000)
(1003, 103, 'SIGNED', NULL, 1200000.00, NULL, NULL, '020', '990-123456-789',
 DATE_SUB(NOW(), INTERVAL 7 DAY), NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),

-- 4. 서명 완료 (SIGNED) - Contract 104 (2,500,000)
(1004, 104, 'SIGNED', NULL, 2500000.00, NULL, NULL, '020', '110-222-333444',
 DATE_SUB(NOW(), INTERVAL 20 DAY), NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),

-- 5. 서명 완료 (SIGNED) - Contract 105 (650,000)
(1005, 105, 'SIGNED', NULL, 650000.00, NULL, NULL, '020', '001-55-666777',
 DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

-- 6. 거래 생성 (CREATED) - Contract 106 (1,500,000)
(1006, 106, 'CREATED', NULL, 1500000.00, NULL, NULL, '020', '990-888999-000',
 NULL, NULL, NULL, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW());
