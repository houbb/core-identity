<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-800 mb-6">仪表盘</h1>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="text-center">
          <el-statistic title="用户总数" :value="1024" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="text-center">
          <el-statistic title="活跃用户" :value="856" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="text-center">
          <el-statistic title="今日登录" :value="128" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="text-center">
          <el-statistic title="待处理" :value="3" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>