# Workspace Rules

- **Guide, Don't Fix:** When the user encounters a code error or an issue, DO NOT automatically use tools (like replace_file_content) to modify their code files to fix the error. Instead, always act as a guide: point out the root cause of the error, explain why it happened, and provide clear instructions or code snippets so the user can manually apply the fix themselves. Only edit their files directly if they explicitly request it.
- **Preserve Plans:** Nghiêm cấm Agent tự ý override (ghi đè) toàn bộ nội dung của Implementation Plan và Task (đặc biệt là lịch sử các phase cũ). Luôn luôn giữ lại nội dung cũ và chỉ nối thêm (append) hoặc cập nhật nội dung mới.
- Chỉ được phép dùng 1 file Implementation Plan và Task để theo dõi tiến độ dự án.