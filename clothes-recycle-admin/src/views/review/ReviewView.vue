<template>
  <div class="review-page">
    <h2>评价管理</h2>

    <!-- 评价列表 -->
    <el-table :data="reviews" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="订单号" width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="orderDetailRef.open(row.orderId)">{{ row.orderNo }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="collectorName" label="回收员" width="100" />
      <el-table-column label="准时度" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.punctualityScore" disabled />
        </template>
      </el-table-column>
      <el-table-column label="态度" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.attitudeScore" disabled />
        </template>
      </el-table-column>
      <el-table-column label="称重规范" width="150">
        <template #default="{ row }">
          <el-rate v-model="row.weighingScore" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="评价时间" width="170" />
    </el-table>

    <!-- 订单详情弹窗 -->
    <OrderDetailDialog ref="orderDetailRef" />

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadReviews"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReviews } from '../../api/review'
import OrderDetailDialog from '../../components/OrderDetailDialog.vue'

const orderDetailRef = ref(null)
const reviews = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function loadReviews() {
  loading.value = true
  try {
    const res = await getReviews({ page: page.value, size: size.value })
    reviews.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(() => loadReviews())
</script>

<style scoped>
.review-page {
  padding: 20px;
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
