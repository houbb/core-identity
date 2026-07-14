<template>
  <div class="sidebar">
    <div class="sidebar-logo">⚙️ Identity Admin</div>
    <ul class="sidebar-nav">
      <li><router-link to="/admin/system" exact>系统概览</router-link></li>
      <li><router-link to="/admin/system/health">服务健康</router-link></li>
      <li><router-link to="/admin/system/contracts">接口契约</router-link></li>
    </ul>
    <div style="position: absolute; bottom: 20px; left: 20px; right: 20px;">
      <small>Core Identity Admin v0.1.0</small>
    </div>
  </div>
  <div class="main-content">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>