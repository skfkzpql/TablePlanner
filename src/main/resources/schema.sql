CREATE DATABASE IF NOT EXISTS table_planner;

USE table_planner;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)                       NOT NULL UNIQUE,
    password   VARCHAR(100)                      NOT NULL,
    email      VARCHAR(100)                      NOT NULL UNIQUE,
    role       ENUM ('ADMIN', 'USER', 'PARTNER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP
);

-- 매장 테이블
CREATE TABLE IF NOT EXISTS stores
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 예약 테이블
CREATE TABLE IF NOT EXISTS reservations
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT                                                                        NOT NULL,
    store_id         BIGINT                                                                        NOT NULL,
    reservation_time TIMESTAMP                                                                     NOT NULL,
    status           ENUM ('Pending', 'Approved', 'Rejected', 'Cancelled', 'Completed', 'Overdue') NOT NULL DEFAULT 'Pending',
    created_at       TIMESTAMP                                                                              DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);

-- 리뷰 테이블
CREATE TABLE IF NOT EXISTS reviews
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    store_id   BIGINT NOT NULL,
    rating     INT CHECK (rating >= 1 AND rating <= 5),
    comment    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE CASCADE
);
