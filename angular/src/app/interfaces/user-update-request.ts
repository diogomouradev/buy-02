export interface UserUpdateRequest {
  name?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  avatar?: File;
}
