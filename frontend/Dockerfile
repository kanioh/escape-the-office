# ---- ビルドステージ ----
FROM node:24-alpine AS build
WORKDIR /app

# 依存のインストールとソースのコピーを別レイヤーに分ける。
# ソースだけ変更したときに npm ci を走らせ直さないため
COPY package.json package-lock.json ./
RUN npm ci

# NEXT_PUBLIC_ はビルド時にJSへ埋め込まれるため、実行時ではなくここで渡す。
# 値はブラウザから見た接続先なので、コンテナ名ではなく localhost になる
ARG NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

COPY . .
RUN npm run build

# ---- 実行ステージ ----
FROM node:24-alpine
WORKDIR /app
# 本番実行であることをライブラリ側に伝える。ビルドステージで設定すると
# npm ci が devDependencies を飛ばし、next build が失敗する
ENV NODE_ENV=production

# Alpine は BusyBox のため useradd ではなく adduser/addgroup を使う
RUN addgroup -S nodejs && adduser -S nextjs -G nodejs

# standalone には public と .next/static が含まれないため、個別にコピーする
COPY --from=build --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=build --chown=nextjs:nodejs /app/.next/static ./.next/static
COPY --from=build --chown=nextjs:nodejs /app/public ./public

USER nextjs

EXPOSE 3000
# server.js の既定値も 0.0.0.0 だが、環境に依存しないよう明示する
ENV PORT=3000 HOSTNAME=0.0.0.0
CMD ["node", "server.js"]
