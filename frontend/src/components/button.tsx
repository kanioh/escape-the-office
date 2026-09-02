import type { ButtonHTMLAttributes } from "react";

/**
 * フォームの主ボタン。3つのフォームで同じ見た目のため、ここに一本化する。
 * border-transparent は色のためではなく、枠線を持つ入力欄と高さ（38px）を揃えるために置く。
 */
export function Button({
  className,
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement>) {
  // className だけ取り出して連結する。そのまま渡すと <button> に className が
  // 2回並び、後の指定が勝って呼び出し側の指定が無言で消える
  return (
    <button
      {...props}
      className={`rounded-full border border-transparent bg-foreground px-5 py-2 text-sm font-medium text-background transition-colors enabled:hover:bg-muted disabled:opacity-40 ${className ?? ""}`}
    />
  );
}
