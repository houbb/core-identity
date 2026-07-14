<template>
  <div class="min-h-screen flex flex-col bg-gray-50">
    <!-- Header -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
        <el-link href="/" :underline="false" class="text-lg font-bold text-blue-600">
          Core Identity
        </el-link>
        <nav class="flex items-center gap-4">
          <el-link href="/" :underline="false" type="primary">首页</el-link>
          <el-link href="/login" :underline="false" type="primary">登录</el-link>
          <el-link href="/register" :underline="false" type="success">注册</el-link>
        </nav>
      </div>
    </header>

    <!-- Content -->
    <main class="flex-1 max-w-6xl mx-auto w-full px-4 py-6">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 py-4 text-center text-sm text-gray-400">
      Core Platform &copy; 2026
    </footer>
  </div>
</template>

<script setup lang="ts">
</script>