<template>
  <div class="admin-layout h-screen flex">
    <!-- Sidebar -->
    <el-menu
      :default-active="route.path"
      class="w-56 border-r border-gray-200 flex-shrink-0"
      router
      background-color="#1e293b"
      text-color="#94a3b8"
      active-text-color="#60a5fa"
    >
      <div class="h-14 flex items-center justify-center text-white font-bold text-lg border-b border-gray-700">
        Core Admin
      </div>
      <el-menu-item index="/">
        <el-icon><Monitor /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      <el-menu-item index="/users">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
    </el-menu>

    <!-- Main area -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Header -->
      <header class="h-14 bg-white border-b border-gray-200 flex items-center px-6 flex-shrink-0">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="ml-auto flex items-center gap-3">
          <el-avatar :size="32" icon="UserFilled" />
          <span class="text-sm text-gray-600">管理员</span>
        </div>
      </header>

      <!-- Content -->
      <main class="flex-1 overflow-auto bg-gray-50 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>