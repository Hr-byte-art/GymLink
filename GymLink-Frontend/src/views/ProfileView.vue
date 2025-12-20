<template>
  <AppLayout>
    <div class="profile-container">
      <div class="profile-header">
        <div class="profile-banner">
          <div class="profile-info">
            <div class="avatar-wrapper" :class="{ 'uploading': loading.avatar }">
              <label class="avatar-clickable">
                <img :src="userDetailInfo?.avatar || authStore.user?.avatar || '/avatar-placeholder.svg'" :alt="displayName" class="user-avatar" />
                <input type="file" accept="image/*" @change="handleAvatarChange" :disabled="loading.avatar" hidden />
                <div class="avatar-overlay">
                  <span v-if="loading.avatar">上传中...</span>
                  <span v-else>点击更换</span>
                </div>
              </label>
            </div>
            <div class="user-details">
              <h1 class="username">{{ displayName }}</h1>
              <p class="user-role">用户身份: {{ getUserRoleText }}</p>
              <p class="join-date">加入时间: {{ userDetailInfo?.createTime ? formatDate(userDetailInfo.createTime) : '加载中...' }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-content">
        <div class="profile-sidebar">
          <el-menu :default-active="activeMenu" class="profile-menu" @select="handleMenuSelect">
            <el-menu-item index="basic">
              <span>基本信息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="courses" v-if="isStudent">
              <span>我的课程</span>
            </el-menu-item>
            <el-menu-item index="appointments" v-if="isStudent">
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item index="favorites">
              <span>我的收藏</span>
            </el-menu-item>
            <el-menu-item index="announcements">
              <span>公告通知</span>
            </el-menu-item>
            <el-menu-item index="notifications">
              <el-badge v-if="notificationUnreadCount > 0" :value="notificationUnreadCount" class="menu-badge">
                <span>消息通知</span>
              </el-badge>
              <span v-else>消息通知</span>
            </el-menu-item>
            <el-menu-item index="coach-appointments" v-if="isCoach">
              <el-badge v-if="pendingAppointmentCount > 0" :value="pendingAppointmentCount" class="menu-badge">
                <span>预约管理</span>
              </el-badge>
              <span v-else>预约管理</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="profile-main">
          <!-- 基本信息 - 学员 -->
          <div v-if="activeMenu === 'basic' && isStudent" class="profile-section">
            <h2>基本信息</h2>
            <el-form :model="studentForm" :rules="studentFormRules" ref="studentFormRef" label-width="120px"
              class="user-info-form" v-loading="loading.userInfo">
              <el-form-item label="用户名">
                <el-input v-model="studentForm.username" disabled></el-input>
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="studentForm.name"></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="studentForm.phone"></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="studentForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="身高(cm)">
                <el-input-number v-model="studentForm.height" :min="0" :max="300" :precision="1"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="体重(kg)">
                <el-input-number v-model="studentForm.weight" :min="0" :max="500" :precision="1"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="账户余额">
                <div class="balance-row">
                  <el-input :model-value="'¥' + (studentForm.balance || 0)" disabled style="width: 150px;"></el-input>
                  <el-button type="success" @click="openTopUpDialog">充值</el-button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateStudentInfo" :loading="loading.submit">保存信息</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 基本信息 - 教练 -->
          <div v-if="activeMenu === 'basic' && isCoach" class="profile-section">
            <h2>基本信息</h2>
            <el-form :model="coachForm" :rules="coachFormRules" ref="coachFormRef" label-width="120px"
              class="user-info-form" v-loading="loading.userInfo">
              <el-form-item label="用户名">
                <el-input v-model="coachForm.username" disabled></el-input>
              </el-form-item>
              <el-form-item label="真实姓名" prop="name">
                <el-input v-model="coachForm.name"></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="coachForm.phone"></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="coachForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="年龄">
                <el-input-number v-model="coachForm.age" :min="18" :max="100"
                  controls-position="right"></el-input-number>
              </el-form-item>
              <el-form-item label="专业方向" prop="specialty">
                <el-input v-model="coachForm.specialty"></el-input>
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input v-model="coachForm.intro" type="textarea" :rows="3"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateCoachInfo" :loading="loading.submit">保存信息</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 安全设置 -->
          <div v-if="activeMenu === 'security'" class="profile-section">
            <h2>安全设置</h2>
            <el-form :model="securityForm" :rules="securityRules" ref="securityFormRef" label-width="120px"
              class="security-form">
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="securityForm.oldPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="securityForm.newPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="checkedPassword">
                <el-input v-model="securityForm.checkedPassword" type="password" show-password></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword" :loading="loading.password">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的课程 -->
          <div v-if="activeMenu === 'courses'" class="profile-section">
            <h2>我的课程</h2>
            <div class="courses-filter">
              <el-input v-model="courseSearchKeyword" placeholder="搜索课程名称" clearable style="width: 200px; margin-right: 15px;" @clear="loadPurchasedCourses" @keyup.enter="loadPurchasedCourses" />
              <el-select v-model="courseStatusFilter" placeholder="订单状态" clearable style="width: 140px; margin-right: 15px;" @change="loadPurchasedCourses">
                <el-option label="已支付" :value="1"></el-option>
                <el-option label="已退款" :value="2"></el-option>
                <el-option label="退款申请中" :value="3"></el-option>
              </el-select>
              <el-button type="primary" @click="loadPurchasedCourses">搜索</el-button>
            </div>
            <div v-loading="loading.purchasedCourses" class="purchased-courses-list">
              <div v-if="purchasedCourses.length > 0" class="courses-grid">
                <div v-for="course in purchasedCourses" :key="course.orderId" class="purchased-course-card" @click="viewCourseDetail(course.courseId)">
                  <div class="course-image">
                    <img :src="course.courseImage || '/course1.svg'" :alt="course.courseName" />
                    <el-tag class="status-tag" :type="course.status === 1 ? 'success' : course.status === 3 ? 'warning' : 'info'">{{ course.status === 1 ? '已购买' : course.status === 3 ? '退款申请中' : '已退款' }}</el-tag>
                  </div>
                  <div class="course-info">
                    <h3 class="course-name">{{ course.courseName }}</h3>
                    <div class="course-meta">
                      <span class="meta-item"><img src="/duration.svg" class="meta-icon" />{{ course.duration }}分钟</span>
                      <span class="meta-item"><img src="/star.svg" class="meta-icon" />{{ course.difficulty }}</span>
                    </div>
                    <div class="course-coach">授课教练：{{ course.coachName || '未知' }}</div>
                    <div class="course-price">购买价格：¥{{ course.price }}</div>
                    <div class="course-time">购买时间：{{ formatDate(course.purchaseTime) }}</div>
                    <div class="course-actions">
                      <el-button type="primary" size="small" @click.stop="viewCourseDetail(course.courseId)">查看详情</el-button>
                      <el-button v-if="course.status === 1 && course.canReview" type="success" size="small" @click.stop="openReviewDialog(course)">评价课程</el-button>
                      <el-tag v-else-if="course.status === 1 && !course.canReview" type="info" size="small">已评价</el-tag>
                      <el-button v-if="course.status === 1" type="warning" size="small" @click.stop="handleRefundCourse(course)">申请退款</el-button>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无已购课程" />
            </div>
            <el-pagination v-if="purchasedCoursesPagination.total > 0" layout="prev, pager, next" :total="purchasedCoursesPagination.total" :page-size="purchasedCoursesPagination.pageSize" :current-page="purchasedCoursesPagination.currentPage" @current-change="handlePurchasedCoursesPageChange" class="pagination" />
          </div>

          <!-- 我的预约 -->
          <div v-if="activeMenu === 'appointments'" class="profile-section">
            <h2>我的预约</h2>
            <el-tabs v-model="appointmentTab" @tab-change="handleAppointmentTabChange">
              <el-tab-pane label="教练预约" name="coach">
                <el-table :data="coachAppointments" style="width: 100%" v-loading="loading.coachAppointments">
                  <el-table-column prop="coachName" label="教练"></el-table-column>
                  <el-table-column label="开始时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.appointTime) }}</template>
                  </el-table-column>
                  <el-table-column label="结束时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
                  </el-table-column>
                  <el-table-column prop="message" label="备注"></el-table-column>
                  <el-table-column label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getCoachAppointmentStatusType(scope.row.status)">{{
                        getCoachAppointmentStatusText(scope.row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="180">
                    <template #default="scope">
                      <el-button v-if="scope.row.status === 0" size="small" type="danger"
                        @click="handleCancelCoachAppointment(scope.row.id)">取消</el-button>
                      <el-button v-if="scope.row.status === 1 && scope.row.canReview" size="small" type="success"
                        @click="openAppointmentReviewDialog(scope.row)">评价</el-button>
                      <el-tag v-else-if="scope.row.status === 1 && !scope.row.canReview" type="info" size="small">已评价</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination v-if="coachAppointmentPagination.total > 0" layout="prev, pager, next"
                  :total="coachAppointmentPagination.total" :page-size="coachAppointmentPagination.pageSize"
                  :current-page="coachAppointmentPagination.currentPage"
                  @current-change="handleCoachAppointmentPageChange" class="pagination" />
                <el-empty v-if="!loading.coachAppointments && coachAppointments.length === 0" description="暂无教练预约记录" />
              </el-tab-pane>
              <el-tab-pane label="器材预约" name="equipment">
                <el-table :data="equipmentReservations" style="width: 100%" v-loading="loading.equipmentReservations">
                  <el-table-column prop="equipmentName" label="器材名称"></el-table-column>
                  <el-table-column label="开始时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.startTime) }}</template>
                  </el-table-column>
                  <el-table-column label="结束时间" width="180">
                    <template #default="scope">{{ formatDateTime(scope.row.endTime) }}</template>
                  </el-table-column>
                  <el-table-column label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getEquipmentReservationStatusType(scope.row.status)">{{
                        getEquipmentReservationStatusText(scope.row.status) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button v-if="scope.row.status === 1" size="small" type="danger"
                        @click="handleCancelEquipmentReservation(scope.row.id)">取消</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination v-if="equipmentReservationPagination.total > 0" layout="prev, pager, next"
                  :total="equipmentReservationPagination.total" :page-size="equipmentReservationPagination.pageSize"
                  :current-page="equipmentReservationPagination.currentPage"
                  @current-change="handleEquipmentReservationPageChange" class="pagination" />
                <el-empty v-if="!loading.equipmentReservations && equipmentReservations.length === 0"
                  description="暂无器材预约记录" />
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 我的收藏 -->
          <div v-if="activeMenu === 'favorites'" class="profile-section">
            <h2>我的收藏</h2>
            <el-tabs v-model="favoriteTab" @tab-change="handleFavoriteTabChange">
              <el-tab-pane label="课程" name="course">
                <div v-loading="loading.favorites" class="favorites-grid">
                  <div v-if="favoriteCourses.length > 0" class="favorites-list">
                    <div v-for="item in favoriteCourses" :key="item.id" class="favorite-card" @click="goToDetail('course', item.targetId)">
                      <div class="favorite-image">
                        <img :src="item.targetImage || '/course1.svg'" :alt="item.targetName" />
                      </div>
                      <div class="favorite-info">
                        <h3 class="favorite-name">{{ item.targetName }}</h3>
                        <p class="favorite-desc">{{ item.targetDescription || '暂无描述' }}</p>
                        <div class="favorite-actions">
                          <el-button size="small" type="danger" @click.stop="handleRemoveFavorite(item, 3)">取消收藏</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无收藏的课程" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="教练" name="coach">
                <div v-loading="loading.favorites" class="favorites-grid">
                  <div v-if="favoriteCoaches.length > 0" class="favorites-list">
                    <div v-for="item in favoriteCoaches" :key="item.id" class="favorite-card" @click="goToDetail('coach', item.targetId)">
                      <div class="favorite-image avatar">
                        <img :src="item.targetImage || '/avatar-placeholder.svg'" :alt="item.targetName" />
                      </div>
                      <div class="favorite-info">
                        <h3 class="favorite-name">{{ item.targetName }}</h3>
                        <p class="favorite-desc">{{ item.targetDescription || '暂无简介' }}</p>
                        <div class="favorite-actions">
                          <el-button size="small" type="danger" @click.stop="handleRemoveFavorite(item, 2)">取消收藏</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无收藏的教练" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="器材" name="equipment">
                <div v-loading="loading.favorites" class="favorites-grid">
                  <div v-if="favoriteEquipments.length > 0" class="favorites-list">
                    <div v-for="item in favoriteEquipments" :key="item.id" class="favorite-card" @click="goToDetail('equipment', item.targetId)">
                      <div class="favorite-image">
                        <img :src="item.targetImage || '/feature1.svg'" :alt="item.targetName" />
                      </div>
                      <div class="favorite-info">
                        <h3 class="favorite-name">{{ item.targetName }}</h3>
                        <p class="favorite-desc">{{ item.targetDescription || '暂无描述' }}</p>
                        <div class="favorite-actions">
                          <el-button size="small" type="danger" @click.stop="handleRemoveFavorite(item, 1)">取消收藏</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无收藏的器材" />
                </div>
              </el-tab-pane>
              <el-tab-pane label="食谱" name="recipe">
                <div v-loading="loading.favorites" class="favorites-grid">
                  <div v-if="favoriteRecipes.length > 0" class="favorites-list">
                    <div v-for="item in favoriteRecipes" :key="item.id" class="favorite-card" @click="goToDetail('recipe', item.targetId)">
                      <div class="favorite-image">
                        <img :src="item.targetImage || '/feature2.svg'" :alt="item.targetName" />
                      </div>
                      <div class="favorite-info">
                        <h3 class="favorite-name">{{ item.targetName }}</h3>
                        <p class="favorite-desc">{{ item.targetDescription || '暂无描述' }}</p>
                        <div class="favorite-actions">
                          <el-button size="small" type="danger" @click.stop="handleRemoveFavorite(item, 4)">取消收藏</el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="暂无收藏的食谱" />
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 公告通知 -->
          <div v-if="activeMenu === 'announcements'" class="profile-section">
            <h2>公告通知</h2>
            <div v-loading="loading.announcements" class="announcements-list">
              <div v-if="announcementList.length > 0">
                <div v-for="item in announcementList" :key="item.id" class="announcement-card">
                  <div class="announcement-card-header">
                    <h3 class="announcement-card-title">{{ item.title }}</h3>
                    <span class="announcement-card-time">{{ formatDate(item.createTime) }}</span>
                  </div>
                  <div class="announcement-card-content">{{ item.content }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无公告" />
            </div>
            <el-pagination v-if="announcementPagination.total > 0" layout="prev, pager, next"
              :total="announcementPagination.total" :page-size="announcementPagination.pageSize"
              :current-page="announcementPagination.currentPage"
              @current-change="handleAnnouncementPageChange" class="pagination" />
          </div>

          <!-- 消息通知 -->
          <div v-if="activeMenu === 'notifications'" class="profile-section">
            <h2>消息通知</h2>
            <div class="notifications-header">
              <el-button v-if="notificationUnreadCount > 0" type="primary" link @click="handleMarkAllNotificationsRead">全部标为已读</el-button>
            </div>
            <div v-loading="loading.notifications" class="notifications-list">
              <div v-if="notificationList.length > 0">
                <div v-for="item in notificationList" :key="item.id" class="notification-card" :class="{ unread: item.isRead === 0 }" @click="handleNotificationClick(item)">
                  <div class="notification-card-icon">
                    <el-icon v-if="item.type === 1" color="#409eff" :size="24"><Calendar /></el-icon>
                    <el-icon v-else color="#67c23a" :size="24"><Bell /></el-icon>
                  </div>
                  <div class="notification-card-content">
                    <div class="notification-card-header">
                      <h3 class="notification-card-title">{{ item.title }}</h3>
                      <span class="notification-card-time">{{ formatDate(item.createTime) }}</span>
                    </div>
                    <div class="notification-card-text">{{ item.content }}</div>
                  </div>
                  <div v-if="item.isRead === 0" class="notification-unread-dot"></div>
                </div>
              </div>
              <el-empty v-else description="暂无消息" />
            </div>
            <el-pagination v-if="notificationPagination.total > 0" layout="prev, pager, next"
              :total="notificationPagination.total" :page-size="notificationPagination.pageSize"
              :current-page="notificationPagination.currentPage"
              @current-change="handleNotificationPageChange" class="pagination" />
          </div>

          <!-- 预约管理 - 教练 -->
          <div v-if="activeMenu === 'coach-appointments' && isCoach" class="profile-section">
            <h2>预约管理</h2>
            <div class="appointments-filter">
              <el-select v-model="coachAppointmentStatusFilter" placeholder="预约状态" clearable style="width: 140px; margin-right: 15px;" @change="loadCoachAppointmentList">
                <el-option label="待确认" :value="0"></el-option>
                <el-option label="已确认" :value="1"></el-option>
                <el-option label="已拒绝" :value="2"></el-option>
              </el-select>
              <el-button type="primary" @click="loadCoachAppointmentList">刷新</el-button>
            </div>
            <div v-loading="loading.coachAppointmentList" class="coach-appointments-list">
              <el-table v-if="coachAppointmentList.length > 0" :data="coachAppointmentList" stripe style="width: 100%">
                <el-table-column prop="studentName" label="学员" width="100" />
                <el-table-column label="预约时间" min-width="180">
                  <template #default="{ row }">
                    <div>{{ formatDateTime(row.appointTime) }}</div>
                    <div class="time-to">至 {{ formatDateTime(row.endTime) }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="message" label="留言" min-width="150">
                  <template #default="{ row }">
                    <span>{{ row.message || '无' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getAppointmentStatusType(row.status)">{{ getAppointmentStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="{ row }">
                    <template v-if="row.status === 0">
                      <el-button size="small" type="success" @click="handleConfirmAppointment(row.id)">确认</el-button>
                      <el-button size="small" type="danger" @click="handleRejectAppointment(row.id)">拒绝</el-button>
                    </template>
                    <el-tag v-else type="info" size="small">已处理</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无预约记录" />
            </div>
            <el-pagination v-if="coachAppointmentPagination.total > 0" layout="prev, pager, next" :total="coachAppointmentPagination.total" :page-size="coachAppointmentPagination.pageSize" :current-page="coachAppointmentPagination.currentPage" @current-change="handleCoachAppointmentListPageChange" class="pagination" />
          </div>
        </div>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog v-model="topUpDialogVisible" title="账户充值" width="400px" :close-on-click-modal="false">
      <el-form :model="topUpForm" label-width="80px">
        <el-form-item label="当前余额">
          <span class="current-balance">¥{{ studentForm.balance || 0 }}</span>
        </el-form-item>
        <el-form-item label="充值金额">
          <div class="topup-amounts">
            <el-button v-for="amount in [50, 100, 200, 500, 1000]" :key="amount"
              :type="topUpForm.amount === amount ? 'primary' : 'default'"
              @click="topUpForm.amount = amount">¥{{ amount }}</el-button>
          </div>
        </el-form-item>
        <el-form-item label="自定义">
          <el-input-number v-model="topUpForm.amount" :min="1" :max="10000" :precision="2" style="width: 200px;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topUpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTopUp" :loading="loading.topUp">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewForm.reviewType === 1 ? '评价课程' : '评价教练'" width="500px" :close-on-click-modal="false">
      <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
        <el-form-item v-if="reviewForm.reviewType === 1" label="课程">
          <el-input :model-value="reviewForm.courseName" disabled />
        </el-form-item>
        <el-form-item v-else label="教练">
          <el-input :model-value="reviewForm.coachName" disabled />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" :placeholder="reviewForm.reviewType === 1 ? '请输入您对课程的评价（选填）' : '请输入您对教练的评价（选填）'" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="loading.review">提交评价</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { changePassword } from '@/api/user'
import { getStudentByUserId, updateStudent, uploadStudentAvatar, getPurchasedCourses, refundCourse, studentTopUp, type PurchasedCourse } from '@/api/student'
import { addCourseReview, canReviewCourse, canReviewAppointmentSilent } from '@/api/review'
import { getCoachByUserId, updateCoach, uploadCoachAvatar } from '@/api/coach'
import { listStudentCoachAppointment, listStudentEquipmentReservation, cancelCoachAppointment, cancelEquipmentReservation, listAppointmentsByCoach, confirmCoachAppointment, rejectCoachAppointment, type CoachAppointment, type EquipmentReservation } from '@/api/appointment'
import { getFavoriteList, removeFavorite, FavoriteType, type FavoriteVo } from '@/api/favorite'
import { getAnnouncementList, type Announcement } from '@/api/announcement'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead, type Notification } from '@/api/notification'
import { Calendar, Bell } from '@element-plus/icons-vue'
import AppLayout from '@/components/AppLayout.vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const activeMenu = ref('basic')
const appointmentTab = ref('coach')
const favoriteTab = ref('course')
const studentFormRef = ref()
const coachFormRef = ref()
const securityFormRef = ref()
const userDetailInfo = ref<any>(null)

const studentForm = ref({ id: '' as string | number, username: '', name: '', phone: '', gender: 1, height: 0, weight: 0, balance: 0 })
const coachForm = ref({ id: '' as string | number, username: '', name: '', phone: '', gender: 1, age: 18, specialty: '', intro: '' })
const securityForm = ref({ oldPassword: '', newPassword: '', checkedPassword: '' })

const studentFormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
const coachFormRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  specialty: [{ required: true, message: '请输入专业方向', trigger: 'blur' }]
}
const securityRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 8, message: '密码长度不能少于8位', trigger: 'blur' }],
  checkedPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, {
    validator: (_rule: any, value: string, callback: any) => {
      if (value !== securityForm.value.newPassword) callback(new Error('两次输入的密码不一致'))
      else callback()
    }, trigger: 'blur'
  }]
}

const loading = ref({ userInfo: false, submit: false, password: false, coachAppointments: false, equipmentReservations: false, avatar: false, purchasedCourses: false, review: false, favorites: false, topUp: false, coachAppointmentList: false, announcements: false, notifications: false })
const coachAppointments = ref<(CoachAppointment & { canReview?: boolean })[]>([])
const equipmentReservations = ref<EquipmentReservation[]>([])

// 教练预约管理相关
const coachAppointmentList = ref<CoachAppointment[]>([])
const coachAppointmentStatusFilter = ref<number | undefined>(0) // 默认显示待确认
const coachAppointmentPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })
const pendingAppointmentCount = ref(0)

// 收藏相关状态
const favoriteCourses = ref<FavoriteVo[]>([])
const favoriteCoaches = ref<FavoriteVo[]>([])
const favoriteEquipments = ref<FavoriteVo[]>([])
const favoriteRecipes = ref<FavoriteVo[]>([])
const equipmentReservationPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })

// 公告相关
const announcementList = ref<Announcement[]>([])
const announcementPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })

// 消息通知相关
const notificationList = ref<Notification[]>([])
const notificationPagination = ref({ total: 0, pageSize: 10, currentPage: 1 })
const notificationUnreadCount = ref(0)

// 已购课程相关
const purchasedCourses = ref<(PurchasedCourse & { canReview?: boolean })[]>([])
const purchasedCoursesPagination = ref({ total: 0, pageSize: 6, currentPage: 1 })
const courseSearchKeyword = ref('')
const courseStatusFilter = ref<number | undefined>(undefined)

// 充值相关
const topUpDialogVisible = ref(false)
const topUpForm = ref({ amount: 100 })

// 评价相关
const reviewDialogVisible = ref(false)
const reviewFormRef = ref()
const reviewForm = ref({ 
  courseId: 0, 
  courseName: '', 
  appointmentId: 0,
  coachName: '',
  reviewType: 1, // 1:课程评价 2:预约评价
  rating: 5, 
  content: '' 
})
const reviewRules = {
  rating: [{ required: true, message: '请选择评分', trigger: 'change' }]
}

const user = computed(() => authStore.user)
const displayName = computed(() => userDetailInfo.value?.name || authStore.displayName || user.value?.username || '')
const isStudent = computed(() => { const role = user.value?.role; return role === 'student' || role === 'user' })
const isCoach = computed(() => user.value?.role === 'coach')
const getUserRoleText = computed(() => {
  const roleMap: Record<string, string> = { admin: '管理员', coach: '教练', student: '学员', user: '学员' }
  return roleMap[user.value?.role || 'student'] || '学员'
})

const formatDate = (dateString?: string | Date) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}
const formatDateTime = (dateString?: string | Date) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const handleMenuSelect = (index: string) => { 
  activeMenu.value = index
  if (index === 'appointments') loadCoachAppointments()
  else if (index === 'courses') loadPurchasedCourses()
  else if (index === 'favorites') loadFavorites(FavoriteType.COURSE)
  else if (index === 'coach-appointments') loadCoachAppointmentList()
  else if (index === 'announcements') loadAnnouncements()
  else if (index === 'notifications') loadNotifications()
}

// 查看课程详情
const viewCourseDetail = (courseId: number) => {
  router.push(`/courses/${courseId}`)
}

// 加载已购课程
const loadPurchasedCourses = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) return
  loading.value.purchasedCourses = true
  try {
    const res = await getPurchasedCourses({
      studentId,
      pageNum: purchasedCoursesPagination.value.currentPage,
      pageSize: purchasedCoursesPagination.value.pageSize,
      courseName: courseSearchKeyword.value || undefined,
      status: courseStatusFilter.value
    })
    const courses = res.records || []
    // 检查每个课程是否可以评价
    const coursesWithReviewStatus = await Promise.all(
      courses.map(async (course) => {
        if (course.status === 1) {
          try {
            const canReview = await canReviewCourse(studentId, course.courseId)
            return { ...course, canReview }
          } catch {
            return { ...course, canReview: false }
          }
        }
        return { ...course, canReview: false }
      })
    )
    purchasedCourses.value = coursesWithReviewStatus
    purchasedCoursesPagination.value.total = res.total || 0
  } catch (error) { console.error('获取已购课程失败:', error) }
  finally { loading.value.purchasedCourses = false }
}

const handlePurchasedCoursesPageChange = (page: number) => {
  purchasedCoursesPagination.value.currentPage = page
  loadPurchasedCourses()
}

// 打开充值对话框
const openTopUpDialog = () => {
  topUpForm.value.amount = 100
  topUpDialogVisible.value = true
}

// 处理充值
const handleTopUp = async () => {
  if (!topUpForm.value.amount || topUpForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  loading.value.topUp = true
  try {
    await studentTopUp(studentForm.value.id, topUpForm.value.amount)
    ElMessage.success(`充值成功！已充值 ¥${topUpForm.value.amount}`)
    topUpDialogVisible.value = false
    // 刷新学员信息以更新余额
    const studentInfo = await getStudentByUserId(user.value?.id as number)
    studentForm.value = { ...studentForm.value, ...studentInfo }
  } catch {
    // 错误已在 request 拦截器中处理
  } finally {
    loading.value.topUp = false
  }
}

// 申请退款
const handleRefundCourse = async (course: PurchasedCourse) => {
  try {
    await ElMessageBox.confirm(
      `确定要申请退款吗？申请提交后需等待管理员审核，审核通过后退款金额 ¥${course.price} 将返还到您的账户余额。`,
      '申请退款',
      { confirmButtonText: '提交申请', cancelButtonText: '取消', type: 'warning' }
    )
    await refundCourse(course.orderId)
    ElMessage.success('退款申请已提交，请等待管理员审核')
    loadPurchasedCourses()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 错误已在 request 拦截器中处理
    }
  }
}

// 打开课程评价对话框
const openReviewDialog = (course: PurchasedCourse) => {
  reviewForm.value = {
    courseId: course.courseId,
    courseName: course.courseName,
    appointmentId: 0,
    coachName: '',
    reviewType: 1,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

// 打开预约评价对话框
const openAppointmentReviewDialog = (appointment: CoachAppointment) => {
  reviewForm.value = {
    courseId: 0,
    courseName: '',
    appointmentId: appointment.id,
    coachName: appointment.coachName || '',
    reviewType: 2,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  const valid = await reviewFormRef.value?.validate().catch(() => false)
  if (!valid) return

  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) {
    ElMessage.error('学员信息未加载完成')
    return
  }

  loading.value.review = true
  try {
    if (reviewForm.value.reviewType === 1) {
      // 课程评价
      await addCourseReview({
        studentId,
        courseId: reviewForm.value.courseId,
        reviewType: 1,
        rating: reviewForm.value.rating,
        content: reviewForm.value.content || undefined
      })
      // 刷新课程列表以更新评价状态
      loadPurchasedCourses()
    } else {
      // 预约评价
      await addCourseReview({
        studentId,
        appointmentId: reviewForm.value.appointmentId,
        reviewType: 2,
        rating: reviewForm.value.rating,
        content: reviewForm.value.content || undefined
      })
      // 刷新预约列表以更新评价状态
      loadCoachAppointments()
    }
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '评价提交失败')
  } finally {
    loading.value.review = false
  }
}
const handleAppointmentTabChange = (tab: string) => { if (tab === 'coach') loadCoachAppointments(); else if (tab === 'equipment') loadEquipmentReservations() }

// 头像上传
const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  // 验证文件大小 (最大 5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  loading.value.avatar = true
  try {
    let newAvatarUrl: string
    const role = user.value?.role
    const isStudentRole = role === 'student' || role === 'user'
    
    if (isStudentRole) {
      // 学员上传头像
      const studentId = studentForm.value.id || user.value?.associatedUserId
      if (!studentId) {
        ElMessage.error('学员信息未加载完成')
        return
      }
      newAvatarUrl = await uploadStudentAvatar(studentId, file)
    } else {
      // 教练上传头像
      const coachId = coachForm.value.id || user.value?.associatedUserId
      if (!coachId) {
        ElMessage.error('教练信息未加载完成')
        return
      }
      newAvatarUrl = await uploadCoachAvatar(coachId, file)
    }

    console.log('头像上传返回的URL:', newAvatarUrl)
    
    // 添加时间戳参数强制浏览器刷新图片缓存
    const avatarWithTimestamp = newAvatarUrl + '?t=' + Date.now()
    
    // 直接使用返回的 URL 更新本地显示
    if (newAvatarUrl) {
      if (userDetailInfo.value) {
        userDetailInfo.value = { ...userDetailInfo.value, avatar: avatarWithTimestamp }
      }
      if (authStore.user) {
        authStore.user.avatar = avatarWithTimestamp
      }
    }
    ElMessage.success('头像更新成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    loading.value.avatar = false
    // 清空 input 以便可以再次选择同一文件
    input.value = ''
  }
}

const loadStudentInfo = async () => {
  if (!user.value?.id) return
  loading.value.userInfo = true
  try {
    console.log('loadStudentInfo - userId:', user.value.id)
    const info = await getStudentByUserId(user.value.id)
    console.log('loadStudentInfo - 返回的学员信息:', info)
    if (info && info.id) {
      userDetailInfo.value = info
      studentForm.value = {
        id: info.id, // 使用返回的学员ID，避免大数字精度问题
        username: info.username || user.value.username,
        name: info.name || '',
        phone: info.phone || '',
        gender: info.gender || 1,
        height: info.height || 0,
        weight: info.weight || 0,
        balance: info.balance || 0
      }
    } else {
      console.error('获取学员信息失败: 返回数据为空')
    }
  } catch (error) { console.error('获取学员信息失败:', error) }
  finally { loading.value.userInfo = false }
}

const loadCoachInfo = async () => {
  if (!user.value?.id) return
  loading.value.userInfo = true
  try {
    console.log('loadCoachInfo - userId:', user.value.id)
    const info = await getCoachByUserId(user.value.id)
    console.log('loadCoachInfo - 返回的教练信息:', info)
    if (info && info.id) {
      userDetailInfo.value = info
      coachForm.value = {
        id: info.id, // 使用返回的教练ID
        username: info.username || user.value.username,
        name: info.name || '',
        phone: info.phone || '',
        gender: info.gender || 1,
        age: info.age || 18,
        specialty: info.specialty || '',
        intro: info.intro || ''
      }
    } else {
      console.error('获取教练信息失败: 返回数据为空')
    }
  } catch (error) { console.error('获取教练信息失败:', error) }
  finally { loading.value.userInfo = false }
}

const updateStudentInfo = async () => {
  const valid = await studentFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!studentForm.value.id) {
    ElMessage.error('学员信息未加载完成，请刷新页面重试')
    return
  }
  loading.value.submit = true
  try {
    console.log('updateStudentInfo - id:', studentForm.value.id)
    await updateStudent(studentForm.value.id, { name: studentForm.value.name, gender: studentForm.value.gender, phone: studentForm.value.phone, height: studentForm.value.height, weight: studentForm.value.weight })
    ElMessage.success('信息更新成功')
    if (authStore.user) authStore.user.name = studentForm.value.name
  } catch (error) { console.error('更新学员信息失败:', error) }
  finally { loading.value.submit = false }
}

const updateCoachInfo = async () => {
  const valid = await coachFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!coachForm.value.id) {
    ElMessage.error('教练信息未加载完成，请刷新页面重试')
    return
  }
  loading.value.submit = true
  try {
    console.log('updateCoachInfo - id:', coachForm.value.id)
    await updateCoach(coachForm.value.id, { name: coachForm.value.name, gender: coachForm.value.gender, phone: coachForm.value.phone, age: coachForm.value.age, specialty: coachForm.value.specialty, intro: coachForm.value.intro })
    ElMessage.success('信息更新成功')
    if (authStore.user) authStore.user.name = coachForm.value.name
  } catch (error) { console.error('更新教练信息失败:', error) }
  finally { loading.value.submit = false }
}

const handleChangePassword = async () => {
  const valid = await securityFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value.password = true
  try {
    await changePassword({ oldPassword: securityForm.value.oldPassword, newPassword: securityForm.value.newPassword, checkedPassword: securityForm.value.checkedPassword })
    ElMessage.success('密码修改成功，请重新登录')
    securityForm.value = { oldPassword: '', newPassword: '', checkedPassword: '' }
    authStore.logout()
    router.push('/login')
  } catch (error) { console.error('修改密码失败:', error) }
  finally { loading.value.password = false }
}

const loadCoachAppointments = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) return
  loading.value.coachAppointments = true
  try {
    const res = await listStudentCoachAppointment({ studentId, pageNum: coachAppointmentPagination.value.currentPage, pageSize: coachAppointmentPagination.value.pageSize })
    const appointments = res.records || []
    // 检查每个已确认的预约是否可以评价（静默处理错误）
    const appointmentsWithReviewStatus = await Promise.all(
      appointments.map(async (appointment) => {
        if (appointment.status === 1) {
          try {
            // 使用字符串类型的ID避免大数字精度问题
            const canReview = await canReviewAppointmentSilent(String(studentId), String(appointment.id))
            return { ...appointment, canReview }
          } catch {
            // 静默处理错误，默认不可评价
            return { ...appointment, canReview: false }
          }
        }
        return { ...appointment, canReview: false }
      })
    )
    coachAppointments.value = appointmentsWithReviewStatus
    coachAppointmentPagination.value.total = res.total || 0
  } catch (error) { console.error('获取教练预约记录失败:', error) }
  finally { loading.value.coachAppointments = false }
}

const loadEquipmentReservations = async () => {
  const studentId = studentForm.value.id || user.value?.associatedUserId
  if (!studentId) {
    console.warn('loadEquipmentReservations: studentId为空')
    return
  }
  loading.value.equipmentReservations = true
  try {
    // 确保studentId是数字类型
    const numericStudentId = typeof studentId === 'string' ? parseInt(studentId, 10) : studentId
    console.log('loadEquipmentReservations - studentId:', numericStudentId)
    const res = await listStudentEquipmentReservation({ studentId: numericStudentId, pageNum: equipmentReservationPagination.value.currentPage, pageSize: equipmentReservationPagination.value.pageSize })
    equipmentReservations.value = res.records || []
    equipmentReservationPagination.value.total = res.total || 0
    console.log('loadEquipmentReservations - 获取到记录数:', equipmentReservations.value.length)
  } catch (error) { console.error('获取器材预约记录失败:', error) }
  finally { loading.value.equipmentReservations = false }
}

const handleCancelCoachAppointment = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    await cancelCoachAppointment(id)
    ElMessage.success('预约已取消')
    loadCoachAppointments()
  } catch (error) { if (error !== 'cancel') console.error('取消预约失败:', error) }
}

const handleCancelEquipmentReservation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', { type: 'warning' })
    await cancelEquipmentReservation(id)
    ElMessage.success('预约已取消')
    loadEquipmentReservations()
  } catch (error) { if (error !== 'cancel') console.error('取消预约失败:', error) }
}

const handleCoachAppointmentPageChange = (page: number) => { coachAppointmentPagination.value.currentPage = page; loadCoachAppointments() }
const handleEquipmentReservationPageChange = (page: number) => { equipmentReservationPagination.value.currentPage = page; loadEquipmentReservations() }

const getCoachAppointmentStatusType = (status: number) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info')
const getCoachAppointmentStatusText = (status: number) => ({ 0: '待确认', 1: '已确认', 2: '已拒绝', 3: '已取消' }[status] || '未知')
const getEquipmentReservationStatusType = (status: number) => ({ 1: 'success', 2: 'info', 3: 'success' }[status] || 'info')
const getEquipmentReservationStatusText = (status: number) => ({ 1: '预约成功', 2: '已取消', 3: '已完成' }[status] || '未知')

// 公告相关函数
const loadAnnouncements = async () => {
  loading.value.announcements = true
  try {
    const res = await getAnnouncementList({
      current: announcementPagination.value.currentPage,
      pageSize: announcementPagination.value.pageSize
    })
    announcementList.value = res.records || []
    announcementPagination.value.total = res.total || 0
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value.announcements = false
  }
}

const handleAnnouncementPageChange = (page: number) => {
  announcementPagination.value.currentPage = page
  loadAnnouncements()
}

// 消息通知相关函数
const loadNotifications = async () => {
  if (!user.value?.id) return
  loading.value.notifications = true
  try {
    const res = await getNotificationList(user.value.id, notificationPagination.value.currentPage, notificationPagination.value.pageSize)
    notificationList.value = res.records || []
    notificationPagination.value.total = res.total || 0
  } catch (error) {
    console.error('获取通知列表失败:', error)
  } finally {
    loading.value.notifications = false
  }
}

const loadNotificationUnreadCount = async () => {
  if (!user.value?.id) return
  try {
    notificationUnreadCount.value = await getUnreadCount(user.value.id)
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

const handleNotificationClick = async (item: Notification) => {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      notificationUnreadCount.value = Math.max(0, notificationUnreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const handleMarkAllNotificationsRead = async () => {
  if (!user.value?.id) return
  try {
    await markAllAsRead(user.value.id)
    notificationList.value.forEach(n => n.isRead = 1)
    notificationUnreadCount.value = 0
    ElMessage.success('已全部标为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
  }
}

const handleNotificationPageChange = (page: number) => {
  notificationPagination.value.currentPage = page
  loadNotifications()
}

// 收藏相关函数
const loadFavorites = async (type: FavoriteType) => {
  loading.value.favorites = true
  try {
    const res = await getFavoriteList({ type, pageNum: 1, pageSize: 50 })
    const list = (res as any)?.records || []
    switch (type) {
      case FavoriteType.COURSE:
        favoriteCourses.value = list
        break
      case FavoriteType.COACH:
        favoriteCoaches.value = list
        break
      case FavoriteType.EQUIPMENT:
        favoriteEquipments.value = list
        break
      case FavoriteType.RECIPE:
        favoriteRecipes.value = list
        break
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
  } finally {
    loading.value.favorites = false
  }
}

const handleFavoriteTabChange = (tab: string) => {
  const typeMap: Record<string, FavoriteType> = {
    course: FavoriteType.COURSE,
    coach: FavoriteType.COACH,
    equipment: FavoriteType.EQUIPMENT,
    recipe: FavoriteType.RECIPE
  }
  loadFavorites(typeMap[tab])
}

const handleRemoveFavorite = async (item: FavoriteVo, type: FavoriteType) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', { type: 'warning' })
    await removeFavorite({ targetId: item.targetId, type })
    ElMessage.success('已取消收藏')
    loadFavorites(type)
  } catch (error) {
    if (error !== 'cancel') console.error('取消收藏失败:', error)
  }
}

const goToDetail = (type: string, id: number) => {
  const routeMap: Record<string, string> = {
    course: '/courses/',
    coach: '/coaches/',
    equipment: '/equipment/',
    recipe: '/recipes/'
  }
  router.push(routeMap[type] + id)
}

// 教练预约管理相关函数
const loadCoachAppointmentList = async () => {
  const coachId = coachForm.value.id || user.value?.associatedUserId
  if (!coachId) return
  loading.value.coachAppointmentList = true
  try {
    console.log('loadCoachAppointmentList - coachId:', coachId)
    const res = await listAppointmentsByCoach({
      coachId: coachId, // 直接传，API层会转字符串
      status: coachAppointmentStatusFilter.value,
      pageNum: coachAppointmentPagination.value.currentPage,
      pageSize: coachAppointmentPagination.value.pageSize
    })
    console.log('loadCoachAppointmentList - res:', res)
    coachAppointmentList.value = res.records || []
    coachAppointmentPagination.value.total = res.total || 0
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value.coachAppointmentList = false
  }
}

const loadPendingAppointmentCount = async () => {
  const coachId = coachForm.value.id || user.value?.associatedUserId
  if (!coachId) return
  try {
    const res = await listAppointmentsByCoach({
      coachId: coachId, // 直接传，API层会转字符串
      status: 0, // 待确认
      pageNum: 1,
      pageSize: 1
    })
    pendingAppointmentCount.value = res.total || 0
  } catch (error) {
    console.error('获取待处理预约数量失败:', error)
  }
}

const handleCoachAppointmentListPageChange = (page: number) => {
  coachAppointmentPagination.value.currentPage = page
  loadCoachAppointmentList()
}

const handleConfirmAppointment = async (appointmentId: number) => {
  try {
    await ElMessageBox.confirm('确定要确认该预约吗？', '确认预约', { type: 'info' })
    await confirmCoachAppointment(appointmentId)
    ElMessage.success('预约已确认')
    loadCoachAppointmentList()
    loadPendingAppointmentCount()
  } catch (error) {
    if (error !== 'cancel') console.error('确认预约失败:', error)
  }
}

const handleRejectAppointment = async (appointmentId: number) => {
  try {
    await ElMessageBox.confirm('确定要拒绝该预约吗？', '拒绝预约', { type: 'warning' })
    await rejectCoachAppointment(appointmentId)
    ElMessage.success('预约已拒绝')
    loadCoachAppointmentList()
    loadPendingAppointmentCount()
  } catch (error) {
    if (error !== 'cancel') console.error('拒绝预约失败:', error)
  }
}

const getAppointmentStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getAppointmentStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待确认', 1: '已确认', 2: '已拒绝' }
  return map[status] || '未知'
}

// 处理 URL query 参数切换 tab
const handleQueryTab = () => {
  const tab = route.query.tab as string
  if (tab) {
    activeMenu.value = tab
    // 根据 tab 加载对应数据
    if (tab === 'appointments') {
      loadCoachAppointments()
    } else if (tab === 'coach-appointments') {
      loadCoachAppointmentList()
    } else if (tab === 'notifications') {
      loadNotifications()
    } else if (tab === 'courses') {
      loadPurchasedCourses()
    } else if (tab === 'favorites') {
      loadFavorites(FavoriteType.COURSE)
    } else if (tab === 'announcements') {
      loadAnnouncements()
    }
  }
}

// 监听路由 query 变化
watch(() => route.query.tab, () => {
  handleQueryTab()
})

onMounted(async () => {
  // 确保用户信息已加载，如果没有 associatedUserId 也需要重新获取
  if (!authStore.user || !authStore.user.associatedUserId) {
    await authStore.initAuth()
  }

  // 直接根据 authStore.user.role 判断
  const role = authStore.user?.role
  const isStudentRole = role === 'student' || role === 'user'
  const isCoachRole = role === 'coach'

  if (isStudentRole) {
    await loadStudentInfo()
  } else if (isCoachRole) {
    await loadCoachInfo()
    // 加载待处理预约数量
    loadPendingAppointmentCount()
  }
  
  // 加载未读通知数量
  loadNotificationUnreadCount()
  
  // 处理 URL query 参数
  handleQueryTab()
})
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 140px);
  background-color: #f5f7fa;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 0;
  color: white;
}

.profile-banner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.profile-info {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-right: 30px;
}

.avatar-wrapper.uploading {
  pointer-events: none;
  opacity: 0.7;
}

.avatar-clickable {
  position: relative;
  display: block;
  width: 120px;
  height: 120px;
  cursor: pointer;
}

.user-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  box-sizing: border-box;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-clickable:hover .avatar-overlay {
  opacity: 1;
}

.user-details h1 {
  font-size: 32px;
  margin-bottom: 10px;
  font-weight: 600;
}

.user-role {
  font-size: 18px;
  margin-bottom: 10px;
  opacity: 0.9;
}

.join-date {
  font-size: 18px;
  opacity: 0.8;
}

.profile-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  gap: 30px;
}

.profile-sidebar {
  width: 250px;
  flex-shrink: 0;
}

.profile-menu {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.profile-main {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.profile-section h2 {
  margin-bottom: 20px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.user-info-form,
.security-form {
  max-width: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 992px) {
  .profile-content {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
  }

  .profile-menu {
    display: flex;
    flex-wrap: wrap;
  }

  .profile-menu .el-menu-item {
    flex: 1;
    text-align: center;
  }
}

/* 已购课程样式 */
.courses-filter {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.purchased-courses-list {
  min-height: 200px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.purchased-course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.purchased-course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.purchased-course-card .course-image {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.purchased-course-card .course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.purchased-course-card .status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.purchased-course-card .course-info {
  padding: 15px;
}

.purchased-course-card .course-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.purchased-course-card .course-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 8px;
}

.purchased-course-card .meta-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.purchased-course-card .meta-icon {
  width: 14px;
  height: 14px;
  margin-right: 4px;
  filter: invert(45%) sepia(98%) saturate(1500%) hue-rotate(196deg) brightness(100%) contrast(96%);
}

.purchased-course-card .course-coach,
.purchased-course-card .course-price,
.purchased-course-card .course-time {
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.purchased-course-card .course-price {
  color: #f56c6c;
  font-weight: 500;
}

.purchased-course-card .course-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.purchased-course-card .course-actions .el-button {
  flex: 1;
  height: 24px;
  line-height: 22px;
  padding: 0 8px;
}

.purchased-course-card .course-actions .el-tag {
  flex: 1;
  text-align: center;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  line-height: 22px;
  padding: 0 8px;
  box-sizing: border-box;
}

/* 表格中按钮和标签高度统一 */
.el-table .el-button--small,
.el-table .el-tag--small {
  height: 24px;
  line-height: 22px;
  padding: 0 8px;
}

.el-table .el-tag--small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .profile-header {
    padding: 20px 0;
  }

  .profile-info {
    flex-direction: column;
    text-align: center;
  }

  .avatar-wrapper {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .profile-content {
    padding: 20px 10px;
  }

  .profile-main {
    padding: 20px;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }
}

/* 收藏列表样式 */
.favorites-grid {
  min-height: 200px;
}

.favorites-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.favorite-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.favorite-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.favorite-image {
  height: 140px;
  overflow: hidden;
  background: #f5f7fa;
}

.favorite-image.avatar {
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.favorite-image.avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.favorite-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.favorite-actions {
  margin-top: auto;
}

@media (max-width: 768px) {
  .favorites-list {
    grid-template-columns: 1fr;
  }
}

/* 充值相关样式 */
.balance-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.current-balance {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.topup-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.topup-amounts .el-button {
  min-width: 80px;
}

/* 教练预约管理样式 */


.appointments-filter {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.coach-appointments-list {
  min-height: 200px;
}

.time-to {
  font-size: 12px;
  color: #909399;
}

/* 公告通知样式 */
.announcements-list {
  min-height: 200px;
}

.announcement-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 15px;
  transition: box-shadow 0.3s;
}

.announcement-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.announcement-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.announcement-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.announcement-card-time {
  font-size: 13px;
  color: #909399;
}

.announcement-card-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 消息通知样式 */
.notifications-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.notifications-list {
  min-height: 200px;
}

.notification-card {
  display: flex;
  align-items: flex-start;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 15px;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.notification-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.notification-card.unread {
  background: #ecf5ff;
  border-color: #b3d8ff;
}

.notification-card-icon {
  margin-right: 15px;
  margin-top: 2px;
  flex-shrink: 0;
}

.notification-card-content {
  flex: 1;
  min-width: 0;
}

.notification-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.notification-card-time {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 15px;
}

.notification-card-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.notification-unread-dot {
  width: 10px;
  height: 10px;
  background: #f56c6c;
  border-radius: 50%;
  margin-left: 15px;
  margin-top: 8px;
  flex-shrink: 0;
}

.menu-badge {
  width: 100%;
}

.menu-badge :deep(.el-badge__content) {
  top: 8px;
  right: 15px;
}
</style>
