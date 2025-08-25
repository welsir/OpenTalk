import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userService, type User, type Friend, type LoginRequest, type RegisterRequest, type UpdateUserInfoRequest } from '@/services/user.service.enhanced'

export const useUserStore = defineStore('user', () => {
  // State
  const currentUser = ref<User | null>(null)
  const friends = ref<Friend[]>([])
  const isLoggedIn = ref(false)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const onlineFriends = computed(() => 
    friends.value.filter(friend => friend.status === 'online')
  )

  const offlineFriends = computed(() => 
    friends.value.filter(friend => friend.status === 'offline')
  )

  // Actions
  const login = async (username: string, password: string) => {
    isLoading.value = true
    error.value = null
    
    try {
      const loginRequest: LoginRequest = { username, password }
      const user = await userService.login(loginRequest)
      
      currentUser.value = user
      isLoggedIn.value = true
      localStorage.setItem('currentUser', JSON.stringify(user))
      
      // 登录成功后加载好友列表
      await loadFriends()
    } catch (err) {
      error.value = err instanceof Error ? err.message : '登录失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData: { username: string; password: string; nickname: string; email?: string }) => {
    isLoading.value = true
    error.value = null
    
    try {
      const registerRequest: RegisterRequest = userData
      const user = await userService.register(registerRequest)
      
      currentUser.value = user
      isLoggedIn.value = true
      friends.value = []
      localStorage.setItem('currentUser', JSON.stringify(user))
    } catch (err) {
      error.value = err instanceof Error ? err.message : '注册失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    if (!currentUser.value) return
    
    isLoading.value = true
    
    try {
      await userService.logout(currentUser.value.id)
      
      currentUser.value = null
      isLoggedIn.value = false
      friends.value = []
      localStorage.removeItem('currentUser')
    } catch (err) {
      error.value = err instanceof Error ? err.message : '登出失败'
    } finally {
      isLoading.value = false
    }
  }

  const updateProfile = async (updates: Partial<User>) => {
    if (!currentUser.value) return
    
    isLoading.value = true
    error.value = null
    
    try {
      const updateRequest: UpdateUserInfoRequest = {
        userId: currentUser.value.id,
        ...updates
      }
      
      const updatedUser = await userService.updateUserInfo(updateRequest)
      currentUser.value = updatedUser
      localStorage.setItem('currentUser', JSON.stringify(updatedUser))
    } catch (err) {
      error.value = err instanceof Error ? err.message : '更新资料失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const addFriend = async (friendId: string) => {
    if (!currentUser.value) return
    
    isLoading.value = true
    error.value = null
    
    try {
      await userService.addFriend(currentUser.value.id, friendId)
      // 重新加载好友列表
      await loadFriends()
    } catch (err) {
      error.value = err instanceof Error ? err.message : '添加好友失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const removeFriend = async (friendId: string) => {
    if (!currentUser.value) return
    
    isLoading.value = true
    error.value = null
    
    try {
      await userService.removeFriend(currentUser.value.id, friendId)
      friends.value = friends.value.filter(friend => friend.friendId !== friendId)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '删除好友失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const searchUsers = async (query: string): Promise<User[]> => {
    isLoading.value = true
    error.value = null
    
    try {
      return await userService.searchUser(query)
    } catch (err) {
      error.value = err instanceof Error ? err.message : '搜索失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const loadFriends = async () => {
    if (!currentUser.value) return
    
    try {
      friends.value = await userService.getFriendList(currentUser.value.id)
    } catch (err) {
      console.error('加载好友列表失败:', err)
      error.value = err instanceof Error ? err.message : '加载好友列表失败'
      // 如果加载失败，设置为空数组避免界面异常
      friends.value = []
    }
  }

  // Initialize from localStorage
  const initializeFromStorage = async () => {
    const stored = localStorage.getItem('currentUser')
    if (stored) {
      try {
        const user = JSON.parse(stored)
        currentUser.value = user
        isLoggedIn.value = true
        // 从服务器重新获取最新的用户信息和好友列表
        await refreshUserData()
      } catch (err) {
        console.error('解析存储的用户数据失败:', err)
        localStorage.removeItem('currentUser')
      }
    }
  }

  const refreshUserData = async () => {
    if (!currentUser.value) return
    
    try {
      // 刷新用户信息
      const updatedUser = await userService.getUserInfo(currentUser.value.id)
      currentUser.value = updatedUser
      localStorage.setItem('currentUser', JSON.stringify(updatedUser))
      
      // 刷新好友列表
      await loadFriends()
    } catch (err) {
      console.error('刷新用户数据失败:', err)
      // 如果刷新失败，可能是token过期或用户不存在，清除本地存储
      currentUser.value = null
      isLoggedIn.value = false
      friends.value = []
      localStorage.removeItem('currentUser')
    }
  }

  return {
    // State
    currentUser,
    friends,
    isLoggedIn,
    isLoading,
    error,
    
    // Getters
    onlineFriends,
    offlineFriends,
    
    // Actions
    login,
    register,
    logout,
    updateProfile,
    addFriend,
    removeFriend,
    searchUsers,
    loadFriends,
    initializeFromStorage,
    refreshUserData
  }
})