const baseURL = '/api';

export const environment = {
  production: true,
  placeholder: '../../assets/images/placeholder.png',
  authCheckURL: `${baseURL}/auth/check`,
  loginURL: `${baseURL}/auth/signin`,
  signupURL: `${baseURL}/auth/signup`,
  usersURL: `${baseURL}/users`,
  productsURL: `${baseURL}/products`,
  userProductsURL: `${baseURL}/products/user/`,
  mediaURL: `${baseURL}/media`,
  userMediaURL: `${baseURL}/media/user/`,
  productMediaURL: `${baseURL}/media/product/`,
  cartURL: `${baseURL}/carts`,
  addToCartURL: `${baseURL}/carts/add`,
  ordersURL: `${baseURL}/orders`,
  logoutURL: `${baseURL}/auth/signout`,
};
