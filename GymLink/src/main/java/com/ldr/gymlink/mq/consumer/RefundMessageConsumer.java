package com.ldr.gymlink.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ldr.gymlink.model.entity.User;
import com.ldr.gymlink.model.enums.UserRoleEnum;
import com.ldr.gymlink.mq.config.RabbitMQConfig;
import com.ldr.gymlink.mq.message.RefundMessage;
import com.ldr.gymlink.service.NotificationService;
import com.ldr.gymlink.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 退款消息消费者
 */
@Slf4j
@Component
public class RefundMessageConsumer {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    @RabbitListener(queues = RabbitMQConfig.REFUND_NOTIFICATION_QUEUE)
    public void handleRefundNotification(RefundMessage message) {
        log.info("收到退款通知消息: type={}, orderId={}", message.getType(), message.getOrderId());

        try {
            String title;
            String content;

            switch (message.getType()) {
                case RefundMessage.TYPE_REFUND_APPLY:
                    // 通知所有管理员有新的退款申请
                    title = "新的退款申请";
                    content = String.format("学员 %s 申请退款\n课程：%s\n退款金额：¥%s",
                            message.getStudentName(),
                            message.getCourseName(),
                            message.getRefundAmount());
                    // 获取所有管理员并发送通知
                    notifyAllAdmins(title, content, message.getOrderId());
                    break;

                case RefundMessage.TYPE_REFUND_APPROVED:
                    // 通知学员退款已通过
                    title = "退款申请已通过";
                    content = String.format("您的退款申请已通过\n课程：%s\n退款金额：¥%s 已返还到您的账户余额",
                            message.getCourseName(),
                            message.getRefundAmount());
                    notificationService.createNotification(
                            message.getStudentUserId(),
                            2, // 系统通知类型
                            title,
                            content,
                            message.getOrderId()
                    );
                    break;

                case RefundMessage.TYPE_REFUND_REJECTED:
                    // 通知学员退款被拒绝
                    title = "退款申请被拒绝";
                    content = String.format("您的退款申请被拒绝\n课程：%s\n如有疑问请联系客服",
                            message.getCourseName());
                    notificationService.createNotification(
                            message.getStudentUserId(),
                            2, // 系统通知类型
                            title,
                            content,
                            message.getOrderId()
                    );
                    break;

                default:
                    log.warn("未知的退款消息类型: {}", message.getType());
                    return;
            }

            log.info("退款通知创建成功: type={}, orderId={}", message.getType(), message.getOrderId());

        } catch (Exception e) {
            log.error("处理退款通知消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 通知所有管理员
     */
    private void notifyAllAdmins(String title, String content, Long relatedId) {
        // 查询所有管理员用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getRole, UserRoleEnum.ADMIN.getValue())
                .eq(User::getStatus, 1);
        List<User> admins = userService.list(queryWrapper);

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    2, // 系统通知类型
                    title,
                    content,
                    relatedId
            );
        }
        log.info("已通知 {} 位管理员", admins.size());
    }
}
