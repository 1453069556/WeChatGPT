--用户基本信息表
CREATE TABLE `fans`
(
    `user_id`          varchar(50) NOT NULL COMMENT '用户ID',
    `subscribe_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    `unsubscribe_time` timestamp NULL DEFAULT NULL COMMENT '取关时间',
    `expiration_time`  timestamp NULL DEFAULT NULL COMMENT '会员到期时间',
    `inviter_id`       varchar(50)          DEFAULT NULL COMMENT '邀请人ID',
    `member_level`     tinyint(4) DEFAULT '1' COMMENT '会员级别',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='粉丝表';
--用户属性
CREATE TABLE `user_properties`
(
    `user_id`           varchar(50)    NOT NULL COMMENT '用户ID',
    `invited_count`     int(11) NOT NULL DEFAULT '0' COMMENT '邀请人数',
    `consumption_total` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '累计消费金额',
    `balance`           decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '余额',
    `points`            int(11) NOT NULL DEFAULT '0' COMMENT '积分',
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户实时属性表';
--价格列表
CREATE TABLE `membership_pricing`
(
    `id`        int(3) NOT NULL AUTO_INCREMENT,
    `date`      int(10) NOT NULL COMMENT '会员时长昵称',
    `price`     decimal(10, 2) NOT NULL COMMENT '现价',
    `oPrice`    decimal(10, 2) NOT NULL COMMENT '原价',
    `discounts` varchar(255) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='会员开通金额列表';
--用户协议表
CREATE TABLE `user_agreement`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `svc_agmt` longtext NOT NULL COMMENT '用户协议内容',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='用户协议表';
-- 微信支付订单表
CREATE TABLE `wechat_order`
(
    `order_id`              int(32) NOT NULL AUTO_INCREMENT,
    `order_name`            varchar(100)   NOT NULL,
    `membership_pricing_id` int(3) NOT NULL,
    `user_id`               varchar(32)    NOT NULL,
    `out_trade_no`          varchar(32)    NOT NULL,
    `transaction_id`        varchar(50) DEFAULT NULL,
    `total_fee`             decimal(10, 2) NOT NULL,
    `order_status`          enum('待支付','已支付','已取消') NOT NULL,
    `create_time`           varchar(20)    NOT NULL,
    `pay_time`              varchar(20) DEFAULT NULL,
    PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4;

-- 会员表
CREATE TABLE `member_info`
(
    `user_id`      varchar(50) NOT NULL,
    `member_level` enum('普通会员','银卡会员','金卡会员','钻石会员') NOT NULL,
    `points`       int(11) NOT NULL DEFAULT '0',
    `start_time`   varchar(20) NOT NULL,
    `expire_time`  varchar(20) NOT NULL,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

