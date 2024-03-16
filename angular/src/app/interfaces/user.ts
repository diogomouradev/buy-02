export interface User {
  name: string;
  email: string;
  password?: string;
  jwtToken?: string;
  id: string;
  role: string;
  avatar?: string;
}
