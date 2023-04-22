--用户基本信息表
CREATE TABLE `fans` (
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `subscribe_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `unsubscribe_time` timestamp NULL DEFAULT NULL COMMENT '取关时间',
  `expiration_time` timestamp NULL DEFAULT NULL COMMENT '会员到期时间',
  `inviter_id` int(11) DEFAULT NULL COMMENT '邀请人ID',
  `member_level` tinyint(4) NOT NULL DEFAULT '1' COMMENT '会员级别',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粉丝表';
--用户属性
CREATE TABLE `user_properties` (
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `invited_count` int(11) NOT NULL DEFAULT '0' COMMENT '邀请人数',
  `consumption_total` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费金额',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `points` int(11) NOT NULL DEFAULT '0' COMMENT '积分',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实时属性表';
--价格列表
CREATE TABLE `membership_pricing` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT,
                                      `mem_duration` int(11) NOT NULL COMMENT '会员时长，以天为单位',
                                      `amount` decimal(10,2) NOT NULL COMMENT '会员开通金额，单位为元',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='会员开通金额列表';
--用户协议表
CREATE TABLE `user_agreement` (
                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                  `svc_agmt` longtext NOT NULL COMMENT '用户协议内容',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户协议表';
-- 微信支付订单表
CREATE TABLE wechat_order (
                              order_id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              product_id INT NOT NULL,
                              transaction_id VARCHAR(50),
                              total_fee DECIMAL(10, 2) NOT NULL,
                              order_status ENUM('待支付', '已支付', '已取消') NOT NULL,
                              create_time DATETIME NOT NULL,
                              pay_time DATETIME,
                              cancel_time DATETIME
);

-- 会员表
CREATE TABLE member (
                        user_id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        phone VARCHAR(11) NOT NULL,
                        email VARCHAR(50),
                        member_level ENUM('普通会员', '银卡会员', '金卡会员', '钻石会员') NOT NULL,
                        points INT NOT NULL DEFAULT 0,
                        register_time DATETIME NOT NULL,
                        last_login_time DATETIME,
                        start_time DATETIME,
                        expire_time DATETIME
);

