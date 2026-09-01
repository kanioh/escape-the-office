import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // 実行に必要なファイルだけを .next/standalone に出力させ、
  // node_modules を丸ごと含めずにコンテナ化する
  output: "standalone",
};

export default nextConfig;
