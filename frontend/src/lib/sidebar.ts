// サーバー（layout.tsx）とクライアント（sidebar.tsx）の両方から使う。
// "use client" のファイルに置くと、サーバー側には実体ではなく参照が渡り、
// Cookie 名として使っても一致しなくなる
export const SIDEBAR_COOKIE = "sidebar-collapsed";
