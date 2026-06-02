# MinLish App - Requirement Specification

## 1. Tổng quan dự án

### 1.1 Tên hệ thống

**MinLish App - Ứng dụng hỗ trợ học từ vựng tiếng Anh**

### 1.2 Mục tiêu

MinLish là ứng dụng Android hỗ trợ học từ vựng tiếng Anh theo lộ trình TOEIC/IELTS. Ứng dụng tập trung vào:

- Học từ vựng bằng flashcard.
- Ôn tập bằng Spaced Repetition, thuật toán SM-2.
- Luyện tập bằng trắc nghiệm và điền từ.
- Cho phép người dùng tự tạo bộ từ vựng cá nhân.
- Cho phép import từ vựng từ file CSV.
- Theo dõi tiến độ học tập.
- Nhắc học cơ bản.
- Hỗ trợ phát âm bằng Android TextToSpeech trong MVP.

Hệ thống được xây dựng theo hướng **client-server, online-first** để dễ triển khai MVP:

```text
Android App
Kotlin + Jetpack Compose
        ↓ REST API
Backend API
NestJS
        ↓ Prisma ORM / Raw SQL
MySQL Database
```

### 1.3 Đối tượng người dùng

- Học sinh, sinh viên muốn học từ vựng tiếng Anh.
- Người học TOEIC.
- Người học IELTS.
- Người đi làm muốn học từ vựng theo chủ đề.
- Người dùng muốn tự tạo bộ từ vựng riêng.

### 1.4 Định hướng sản phẩm

MinLish không chỉ là app CRUD từ vựng. Đây là một hệ thống học từ vựng có learning loop rõ ràng:

```text
Đăng ký / đăng nhập
→ Chọn mục tiêu học TOEIC hoặc IELTS
→ Chọn level
→ Chọn deck có sẵn
→ Học flashcard
→ Đánh giá Again / Hard / Good / Easy
→ Backend tính lịch ôn bằng SM-2
→ Ôn tập từ đến hạn
→ Luyện tập bằng quiz / fill in blank
→ Theo dõi tiến độ
```

### 1.5 Phạm vi MVP

MVP tập trung vào các chức năng chính:

- Đăng ký / đăng nhập email + password.
- JWT authentication.
- Hồ sơ người dùng.
- Chọn learning path: TOEIC hoặc IELTS.
- Chọn level theo từng learning path.
- Xem deck hệ thống theo path và level.
- Người dùng tạo deck cá nhân.
- Khi tạo tài khoản, hệ thống tự tạo một deck cá nhân mặc định tên **Favorites**.
- Thêm từ thủ công vào deck cá nhân.
- Import CSV vào deck cá nhân.
- Bấm tim ở bất kỳ từ nào để copy từ đó vào Favorites deck.
- Học flashcard.
- Ôn tập global theo các từ đến hạn.
- Ôn tập theo từng deck.
- Luyện tập trắc nghiệm.
- Luyện tập điền từ.
- Spaced Repetition SM-2.
- Dashboard tiến độ cơ bản.
- Notification nhắc học cơ bản.
- Audio phát âm bằng Android TextToSpeech.

### 1.6 Ngoài phạm vi MVP

Các chức năng có thể phát triển sau:

- Google login.
- Full offline-first.
- Offline CRUD deck/vocabulary.
- Đồng bộ nhiều thiết bị nâng cao.
- Conflict resolution phức tạp.
- Marketplace chia sẻ deck.
- Audio file storage riêng.
- Speech recognition / chấm phát âm.
- AI tạo ví dụ, collocation, related words.
- Import Excel `.xlsx`.
- Email notification đầy đủ.
- Web app dùng chung backend.

---

## 2. Kiến trúc tổng quan

### 2.1 Stack đề xuất

#### Android

- Kotlin
- Jetpack Compose
- Navigation Compose
- MVVM
- Retrofit
- OkHttp
- DataStore
- Android TextToSpeech
- Room, có thể thêm sau cho cache/offline review
- WorkManager, có thể dùng cho notification hoặc sync sau này

#### Backend

- NestJS
- Prisma ORM hoặc raw SQL
- MySQL
- JWT
- bcrypt
- class-validator
- class-transformer
- Multer hoặc middleware upload file
- CSV parser
- Firebase Cloud Messaging, optional/future

#### Database

- MySQL 8+
- InnoDB
- utf8mb4
- Foreign key đầy đủ
- Soft delete cho các bảng nghiệp vụ chính

### 2.2 Nguyên tắc thiết kế

- Backend là source of truth.
- Android không kết nối trực tiếp database.
- Android gọi backend qua REST API.
- MVP ưu tiên online-first.
- Logic SM-2 đặt ở backend trong MVP.
- Android chỉ gửi rating: `AGAIN`, `HARD`, `GOOD`, `EASY`.
- Backend tính `repetition`, `interval_days`, `ease_factor`, `due_at`.
- Deck hệ thống và deck cá nhân phân biệt bằng `deck_type`.
- Favorites không có bảng riêng. Favorites chỉ là một deck cá nhân mặc định được tạo khi user đăng ký.
- Vocabulary vẫn thuộc một deck theo quan hệ `Deck 1 - n Vocabulary`.
- Khi bấm tim, hệ thống copy vocabulary vào Favorites deck.
- Bản copy trong Favorites lưu `source_vocabulary_id` để biết nó được copy từ vocabulary gốc nào.
- Không dùng bảng trung gian many-to-many trong MVP.

---

## 3. Vai trò người dùng

### 3.1 Guest

Có thể:

- Xem màn giới thiệu.
- Đăng ký.
- Đăng nhập.

Không thể:

- Học từ.
- Tạo deck.
- Import từ.
- Xem dashboard.

### 3.2 Authenticated User

Có thể:

- Chọn mục tiêu học TOEIC/IELTS.
- Chọn level.
- Xem deck hệ thống.
- Học flashcard.
- Ôn tập global.
- Ôn tập theo deck.
- Luyện tập trắc nghiệm và điền từ.
- Tạo deck cá nhân.
- Thêm/sửa/xóa từ trong deck cá nhân.
- Import CSV vào deck cá nhân.
- Bấm tim để lưu từ vào Favorites deck.
- Xem tiến độ.
- Cập nhật hồ sơ.

### 3.3 Admin / Content Manager, optional

Có thể phát triển sau:

- Tạo deck hệ thống.
- Quản lý TOEIC/IELTS levels.
- Import bộ từ hệ thống.
- Quản lý nội dung.

Trong MVP, system data có thể được seed bằng SQL/script.

---

## 4. User flows chính

### 4.1 Đăng ký và khởi tạo tài khoản

```text
User mở app
→ Register
→ Nhập full name, email, password
→ Backend hash password bằng bcrypt
→ Backend tạo user
→ Backend tạo notification settings mặc định
→ Backend tạo deck cá nhân mặc định tên "Favorites"
→ Backend trả JWT
→ User chọn TOEIC hoặc IELTS
→ User chọn target level
→ Vào Home Dashboard
```

### 4.2 Đăng nhập

```text
User mở app
→ Login
→ Nhập email/password
→ Backend xác thực
→ Backend trả JWT
→ Android lưu token vào DataStore
→ Chuyển vào Home Dashboard
```

### 4.3 Học deck có sẵn

```text
Home
→ Chọn TOEIC hoặc IELTS
→ Chọn level
→ Xem danh sách deck hệ thống
→ Chọn deck
→ Bắt đầu học flashcard
→ Front: word + pronunciation + audio
→ Back: meaning + example + collocation
→ User chọn Again/Hard/Good/Easy
→ Backend tạo/cập nhật review card
→ Backend ghi review log
→ Chuyển sang từ tiếp theo
```

### 4.4 Tạo deck cá nhân và thêm từ

```text
My Decks
→ Create Deck
→ Nhập tên, mô tả, tags
→ Backend tạo deck rỗng
→ User vào deck detail
→ Chọn một trong hai phương thức thêm từ:
   1. Thêm thủ công
   2. Import CSV
```

### 4.5 Thêm từ thủ công

```text
Deck Detail
→ Add Word
→ User nhập word, meaning, pronunciation, example, collocation...
→ Backend normalize word và meaning
→ Backend check trùng trong cùng deck
→ Nếu không trùng: insert vocabulary
→ Nếu trùng đúng word + meaning: báo từ đã tồn tại
→ Nếu trùng word nhưng khác meaning:
   App hiển thị popup:
   "Từ này đã tồn tại trong deck. Bạn có muốn bổ sung nghĩa mới cho từ này không?"
→ Nếu user đồng ý: insert vocabulary mới với cùng word nhưng meaning khác
```

### 4.6 Import CSV

```text
Deck Detail
→ Import
→ Chọn file CSV
→ App upload file lên backend
→ Backend parse CSV
→ Backend validate rows
→ Backend normalize word và meaning
→ Backend check trùng trong chính file
→ Backend check trùng với vocabulary đang có trong deck
→ Row hợp lệ: insert
→ Row trùng hoặc lỗi: skip
→ Backend trả import report gồm số thành công, số trùng, số lỗi
```

### 4.7 Thêm vào Favorites bằng nút tim

```text
User xem một vocabulary ở bất kỳ deck nào
→ Bấm nút tim
→ Backend tìm Favorites deck của user
→ Backend check trong Favorites đã có vocabulary copy từ source này chưa
→ Nếu chưa có:
   copy vocabulary gốc sang Favorites deck
   set source_vocabulary_id = originalVocabularyId
→ Nếu đã có:
   không insert lại, trả trạng thái already_favorited
```

Bỏ tim:

```text
User bấm lại nút tim
→ Backend tìm vocabulary trong Favorites có source_vocabulary_id = originalVocabularyId
→ Soft delete bản copy trong Favorites
```

### 4.8 Ôn tập global

```text
Home
→ Start Review / Due Today
→ App gọi GET /learning/due
→ Backend lấy review_cards đến hạn của user
→ App hiển thị flashcard review
→ User chọn Again/Hard/Good/Easy
→ Backend cập nhật SM-2
```

### 4.9 Ôn tập theo deck

```text
Deck Detail
→ Review This Deck
→ App gọi GET /learning/due?deckId=<deckId>
→ Backend lấy review_cards đến hạn của user và vocabulary thuộc deck đó
→ App dùng chung ReviewScreen
```

### 4.10 Practice trắc nghiệm

```text
Deck Detail
→ Practice
→ Multiple Choice
→ Backend tạo practice session
→ Backend generate câu hỏi từ vocabulary trong deck
→ User chọn đáp án
→ Backend lưu answer
→ Kết thúc session
→ Backend tính accuracy
```

### 4.11 Practice điền từ

```text
Deck Detail
→ Practice
→ Fill In Blank
→ Backend trả câu ví dụ bị ẩn từ
→ User nhập từ
→ Backend kiểm tra đúng/sai
→ Lưu kết quả
```

---

## 5. Functional Requirements

## 5.1 Authentication Module

### FR-AUTH-01: Register

Input:

- fullName
- email
- password

Validation:

- Email đúng định dạng.
- Email unique.
- Password tối thiểu 8 ký tự.
- Password nên có chữ và số.

Process:

1. Hash password bằng bcrypt.
2. Tạo user.
3. Tạo notification settings mặc định.
4. Tạo deck cá nhân mặc định tên `Favorites`.
5. Trả access token và user profile.

Output:

- accessToken
- refreshToken, optional
- user object

### FR-AUTH-02: Login

Input:

- email
- password

Process:

1. Tìm user theo email.
2. So sánh password với `password_hash`.
3. Nếu đúng, trả JWT.

### FR-AUTH-03: Get current user

`GET /auth/me` trả thông tin user hiện tại dựa vào JWT.

### FR-AUTH-04: Logout

MVP có thể logout phía client bằng cách xóa token khỏi DataStore.

### FR-AUTH-05: Google login, future

Google login không bắt buộc trong MVP.

---

## 5.2 User Profile Module

### FR-USER-01: Xem hồ sơ

Hiển thị:

- fullName
- email
- avatarUrl
- learningGoal
- currentLevel
- targetLevel
- dailyNewWordsGoal
- dailyReminderTime

### FR-USER-02: Cập nhật hồ sơ

Có thể cập nhật:

- fullName
- learningGoal: `TOEIC`, `IELTS`
- currentLevel
- targetLevel
- dailyNewWordsGoal
- timezone
- dailyReminderTime

---

## 5.3 Learning Path & Level Module

### FR-PATH-01: Danh sách learning paths

MVP có:

- TOEIC
- IELTS

### FR-PATH-02: Danh sách levels theo path

Ví dụ:

TOEIC:

- TOEIC 450
- TOEIC 600
- TOEIC 750
- TOEIC 900

IELTS:

- IELTS 4.0
- IELTS 5.5
- IELTS 6.5
- IELTS 7.0+

### FR-PATH-03: Lấy deck theo level

Khi người dùng chọn path và level, app hiển thị các deck hệ thống phù hợp.

---

## 5.4 Deck Management Module

### FR-DECK-01: Xem deck hệ thống

Người dùng có thể xem deck có sẵn theo learning path và level.

Deck hệ thống:

- `deck_type = SYSTEM`
- `owner_user_id = NULL`
- User thường không được sửa/xóa.

### FR-DECK-02: Xem deck cá nhân

Người dùng có thể xem các deck do mình tạo, bao gồm deck mặc định `Favorites`.

### FR-DECK-03: Tạo deck cá nhân

Input:

- name
- description
- tags
- learningLevelId, optional

Process:

- Backend tạo deck rỗng.
- `deck_type = USER`
- `owner_user_id = currentUser.id`
- `is_default = false`

### FR-DECK-04: Favorites deck mặc định

Khi tạo account, hệ thống tự tạo một deck cá nhân:

```text
name = Favorites
deck_type = USER
owner_user_id = user.id
is_default = true
```

Rules:

- Favorites deck không được xóa.
- Favorites deck không cho import file trực tiếp.
- Favorites deck chỉ nhận từ thông qua hành động bấm nút tim.
- Có thể ôn tập và practice Favorites như deck bình thường.
- Có thể hiển thị trong danh sách My Decks hoặc một khu vực riêng.

### FR-DECK-05: Sửa deck cá nhân

User chỉ được sửa deck do mình sở hữu.

Restrictions:

- Không cho sửa system deck.
- Không cho đổi Favorites deck thành deck thường.
- Có thể không cho đổi tên Favorites trong MVP để tránh lỗi flow.

### FR-DECK-06: Xóa deck cá nhân

User chỉ được xóa deck cá nhân không phải default deck.

Nên dùng soft delete.

### FR-DECK-07: Xem chi tiết deck

Hiển thị:

- name
- description
- tags
- totalWords
- learnedWords
- dueWords
- createdAt
- updatedAt

Deck detail có các action:

- Learn New Words
- Review This Deck
- Practice
- Add Word, nếu deck cá nhân thường
- Import CSV, nếu deck cá nhân thường
- Edit Deck, nếu deck cá nhân thường

---

## 5.5 Vocabulary Management Module

### FR-VOCAB-01: Xem danh sách từ trong deck

Mỗi vocabulary gồm:

- id
- deckId
- sourceVocabularyId, nullable
- word
- pronunciation
- meaning
- descriptionEn
- example
- collocation
- relatedWords
- note
- audioUrl, optional/future
- imageUrl, optional/future
- difficulty
- partOfSpeech

### FR-VOCAB-02: Đơn vị học là vocabulary entry

Một `word` có thể có nhiều nghĩa trong cùng hoặc khác deck.

Ví dụ:

```text
charge = tính phí
charge = buộc tội
```

Hai dòng này là hai vocabulary entries khác nhau.

Review và practice luôn dựa trên `vocabulary_id`, không dựa trên `word`.

### FR-VOCAB-03: Thêm từ thủ công

Chỉ áp dụng cho deck cá nhân thường.

Input bắt buộc:

- word
- meaning

Input optional:

- pronunciation
- descriptionEn
- example
- collocation
- relatedWords
- note
- difficulty
- partOfSpeech

Validation:

- word không rỗng.
- meaning không rỗng.
- word không quá 150 ký tự.
- pronunciation không quá 255 ký tự.
- Backend tạo `normalized_word`.
- Backend tạo `normalized_meaning`.

Duplicate handling:

- Duplicate chính xác nếu cùng `deck_id`, cùng `normalized_word`, cùng `normalized_meaning`, và chưa bị soft delete.
- Nếu duplicate chính xác: không insert, trả lỗi `DUPLICATE_VOCABULARY`.
- Nếu cùng `normalized_word` nhưng khác `normalized_meaning`: backend trả cảnh báo `WORD_EXISTS_WITH_DIFFERENT_MEANING`.
- App hiển thị popup: **"Từ này đã tồn tại trong deck. Bạn có muốn bổ sung nghĩa mới cho từ này không?"**
- Nếu user xác nhận: app gửi lại request với flag `allowSameWordDifferentMeaning = true`, backend insert vocabulary mới.

### FR-VOCAB-04: Import CSV

Chỉ áp dụng cho deck cá nhân thường.

CSV template:

```csv
word,pronunciation,meaning,description_en,example,collocation,related_words,note
apple,/ˈæp.əl/,quả táo,A round fruit,I eat an apple every day.,apple pie;green apple,fruit;banana,
```

Rules:

- `word` và `meaning` bắt buộc.
- Các field khác optional.
- `collocation` và `related_words` có thể dùng dấu `;`.
- Backend validate từng row.
- Backend không fail toàn bộ file chỉ vì vài row lỗi hoặc trùng.
- Row trùng sẽ bị skip.
- API trả import report.

Duplicate handling khi import:

- Check trùng theo `deck_id + normalized_word + normalized_meaning`.
- Trùng trong file: skip và tăng `duplicateRows`.
- Trùng với DB: skip và tăng `duplicateRows`.
- Không hiển thị popup trong import.
- Không tự động bổ sung nghĩa nếu row duplicate chính xác.
- Nếu cùng word nhưng khác meaning thì được xem là vocabulary khác và được insert.

Import report cần có:

- totalRows
- successRows
- duplicateRows
- failedRows
- errors[]
- duplicates[]

### FR-VOCAB-05: Favorite bằng nút tim

Favorites không dùng bảng riêng.

Khi user bấm tim ở một vocabulary bất kỳ:

1. Backend tìm Favorites deck của user.
2. Backend check trong Favorites có active vocabulary nào có `source_vocabulary_id = originalVocabularyId` chưa.
3. Nếu chưa có, backend copy vocabulary gốc sang Favorites.
4. Bản copy có:
   - `deck_id = favoritesDeckId`
   - `source_vocabulary_id = originalVocabularyId`
   - các field word/meaning/example/... được copy từ vocabulary gốc
5. Nếu đã có, backend không insert lại.

Khi user bỏ tim:

1. Backend tìm bản copy trong Favorites theo `source_vocabulary_id = originalVocabularyId`.
2. Soft delete bản copy đó.

Important:

- Favorites deck không import trực tiếp.
- Favorites deck không thêm thủ công trong MVP.
- Favorites chỉ được cập nhật qua nút tim.
- Nếu cùng một word nhưng khác meaning ở hai vocabulary gốc khác nhau, user có thể favorite cả hai vì `source_vocabulary_id` khác nhau.
- Nếu bấm tim lại cùng một vocabulary gốc, không tạo bản copy thứ hai.

### FR-VOCAB-06: Sửa từ trong deck cá nhân

User chỉ được sửa từ thuộc deck cá nhân của mình.

Rules:

- Không cho user thường sửa từ trong system deck.
- Có thể cho sửa từ trong Favorites deck hoặc không. MVP khuyến nghị không cho sửa trực tiếp Favorites để tránh lệch với source.
- Nếu sửa word/meaning trong deck cá nhân, backend cần validate duplicate lại.

### FR-VOCAB-07: Xóa từ trong deck cá nhân

User chỉ được xóa từ thuộc deck cá nhân của mình.

Rules:

- Dùng soft delete.
- Không cho user thường xóa từ thuộc system deck.
- Bỏ tim tương đương soft delete bản copy trong Favorites.

### FR-VOCAB-08: Tìm kiếm từ

User có thể tìm theo:

- word
- meaning
- example
- deck

Search trả về `word`, không trả `normalized_word` cho UI.

---

## 5.6 Flashcard Learning Module

### FR-LEARN-01: Start learning deck

User chọn deck và bắt đầu học.

App lấy danh sách vocabulary trong deck theo daily plan hoặc giới hạn số từ mới.

### FR-LEARN-02: Flashcard front

Hiển thị:

- word
- pronunciation
- audio button
- hint/context nếu cần phân biệt nhiều nghĩa

### FR-LEARN-03: Flashcard back

Hiển thị:

- meaning
- descriptionEn
- example
- collocation
- relatedWords
- note

### FR-LEARN-04: Flip animation

App có animation lật flashcard.

### FR-LEARN-05: Rating

Sau khi xem back, user chọn:

- AGAIN
- HARD
- GOOD
- EASY

### FR-LEARN-06: Audio TextToSpeech

MVP dùng Android TextToSpeech.

---

## 5.7 Spaced Repetition Module

### FR-SRS-01: Tạo review card

Khi user bắt đầu học một vocabulary, backend tạo review card nếu chưa có.

Review card gắn với:

- user
- vocabulary
- repetition
- intervalDays
- easeFactor
- dueAt
- lastReviewedAt

### FR-SRS-02: Review theo vocabulary_id

SM-2 state luôn dựa trên:

```text
user_id + vocabulary_id
```

Không dựa trên `word`.

Lý do:

- Cùng một word có thể có nhiều nghĩa.
- Mỗi nghĩa/ngữ cảnh là một vocabulary entry riêng.
- Mỗi vocabulary entry có trạng thái nhớ riêng.

### FR-SRS-03: Rating mapping

- AGAIN = quality 0
- HARD = quality 3
- GOOD = quality 4
- EASY = quality 5

### FR-SRS-04: Due review global

Backend trả danh sách review cards đến hạn:

```text
user_id = current user
due_at <= now
status != SUSPENDED
```

### FR-SRS-05: Due review theo deck

Backend hỗ trợ filter optional theo deck:

```text
GET /learning/due?deckId=<deckId>
```

Query logic:

```text
review_cards.user_id = current user
review_cards.due_at <= now
vocabularies.deck_id = deckId
```

Không cần thêm `deck_id` vào `review_cards`.

### FR-SRS-06: Review log

Mỗi lần review lưu:

- rating
- quality
- oldIntervalDays
- newIntervalDays
- oldEaseFactor
- newEaseFactor
- reviewedAt

---

## 5.8 Practice Module

### FR-PRACTICE-01: Multiple Choice

User luyện tập bằng trắc nghiệm.

Question types:

- Chọn nghĩa đúng của từ.
- Chọn từ đúng theo nghĩa.

MVP có thể random 4 đáp án từ cùng deck hoặc cùng level.

### FR-PRACTICE-02: Fill In Blank

User điền từ còn thiếu trong câu ví dụ.

Ví dụ:

```text
I eat an _____ every day.
Answer: apple
```

### FR-PRACTICE-03: Listening Practice, optional

MVP có thể dùng TextToSpeech:

- App phát âm từ.
- User chọn nghĩa đúng hoặc nhập từ nghe được.

### FR-PRACTICE-04: Practice theo deck

Practice thường bắt đầu từ Deck Detail.

Input:

- deckId
- practiceType
- totalQuestions

### FR-PRACTICE-05: Lưu practice session và answer

Backend lưu:

- practice_sessions
- practice_answers

---

## 5.9 Analytics & Progress Module

### FR-ANALYTICS-01: Dashboard

Hiển thị:

- totalWords
- learnedWords
- dueToday
- newWordsToday
- streak
- accuracy
- totalDecks
- totalPracticeSessions

### FR-ANALYTICS-02: Deck progress

Trong Deck Detail hiển thị:

- totalWords
- learnedWords trong deck
- dueWords trong deck
- progress percentage

### FR-ANALYTICS-03: Daily activity

Hiển thị 7 ngày gần nhất:

- số từ mới
- số từ đã ôn
- số câu đúng
- số câu sai

### FR-ANALYTICS-04: Streak

Một ngày được tính active nếu có ít nhất một hoạt động:

- review flashcard
- học từ mới
- practice quiz

---

## 5.10 Notification Module

### FR-NOTI-01: Daily reminder

User có thể bật/tắt nhắc học hàng ngày.

MVP dễ nhất:

- Android local notification bằng WorkManager/AlarmManager.

### FR-NOTI-02: Due review reminder

MVP:

- Home hiển thị due words.
- Local notification theo giờ nhắc học.

Future:

- Backend gửi FCM.
- Email reminder.

---

## 6. API Specification

## 6.1 Auth APIs

### POST /auth/register

Request:

```json
{
  "fullName": "Nguyen Van A",
  "email": "user@example.com",
  "password": "Password123"
}
```

Response:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Nguyen Van A"
  }
}
```

Backend side effects:

- Create user.
- Create notification settings.
- Create default Favorites deck.

### POST /auth/login

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

### GET /auth/me

Header:

```text
Authorization: Bearer <accessToken>
```

---

## 6.2 User APIs

### GET /users/me

Lấy profile user hiện tại.

### PATCH /users/me

Cập nhật profile.

---

## 6.3 Learning Path APIs

### GET /learning-paths

Trả danh sách TOEIC/IELTS.

### GET /learning-paths/:pathId/levels

Trả levels thuộc path.

### GET /levels/:levelId/decks

Trả decks hệ thống thuộc level.

---

## 6.4 Deck APIs

### GET /decks

Query params:

- type: `SYSTEM`, `USER`, `ALL`
- levelId
- search

### GET /decks/favorites

Trả Favorites deck của current user.

### POST /decks

Tạo deck cá nhân.

Request:

```json
{
  "name": "My Business Words",
  "description": "Từ vựng business tự học",
  "tags": ["Business", "Office"]
}
```

### GET /decks/:id

Chi tiết deck.

### PATCH /decks/:id

Sửa deck cá nhân.

### DELETE /decks/:id

Soft delete deck cá nhân, không áp dụng cho Favorites deck.

---

## 6.5 Vocabulary APIs

### GET /decks/:deckId/vocabularies

Lấy danh sách từ trong deck.

### POST /decks/:deckId/vocabularies

Thêm từ thủ công vào deck cá nhân thường.

Request:

```json
{
  "word": "charge",
  "pronunciation": "/tʃɑːrdʒ/",
  "meaning": "tính phí",
  "descriptionEn": "To ask an amount of money for a service.",
  "example": "The hotel will charge an extra fee.",
  "collocation": "charge a fee; charge extra",
  "relatedWords": "fee;cost;payment",
  "note": "Business context",
  "allowSameWordDifferentMeaning": false
}
```

Duplicate exact response:

```json
{
  "code": "DUPLICATE_VOCABULARY",
  "message": "This vocabulary already exists in this deck."
}
```

Same word different meaning response:

```json
{
  "code": "WORD_EXISTS_WITH_DIFFERENT_MEANING",
  "message": "This word already exists with another meaning. Do you want to add a new meaning?",
  "existingItems": [
    {
      "id": "uuid",
      "word": "charge",
      "meaning": "buộc tội"
    }
  ]
}
```

### PATCH /vocabularies/:id

Sửa vocabulary trong deck cá nhân.

### DELETE /vocabularies/:id

Soft delete vocabulary.

### POST /vocabularies/:id/favorite

Bấm tim để copy vocabulary vào Favorites deck.

Response nếu mới thêm:

```json
{
  "status": "added",
  "favoriteVocabularyId": "uuid"
}
```

Response nếu đã có:

```json
{
  "status": "already_favorited",
  "favoriteVocabularyId": "uuid"
}
```

### DELETE /vocabularies/:id/favorite

Bỏ tim, soft delete bản copy trong Favorites.

### POST /decks/:deckId/import-csv

Upload CSV và import vocabularies.

Response:

```json
{
  "importJobId": "uuid",
  "totalRows": 5000,
  "successRows": 4800,
  "duplicateRows": 150,
  "failedRows": 50,
  "status": "PARTIAL_SUCCESS",
  "duplicates": [
    {
      "row": 12,
      "word": "appointment",
      "meaning": "cuộc hẹn",
      "reason": "Duplicate in this deck"
    }
  ],
  "errors": [
    {
      "row": 20,
      "field": "meaning",
      "message": "Meaning is required"
    }
  ]
}
```

---

## 6.6 Learning APIs

### GET /learning/daily-plan

Response:

```json
{
  "newWordsGoal": 10,
  "newWordsAvailable": 10,
  "dueReviewCount": 15,
  "dueCards": [],
  "newWords": []
}
```

### GET /learning/due

Query params:

- deckId, optional
- limit, optional

Nếu không có `deckId`, trả global due cards.

Nếu có `deckId`, trả due cards trong deck đó.

### POST /learning/review

Request:

```json
{
  "vocabularyId": "uuid",
  "rating": "GOOD",
  "reviewedAt": "2026-05-22T10:00:00.000Z"
}
```

---

## 6.7 Practice APIs

### POST /practice/sessions

Request:

```json
{
  "deckId": "uuid",
  "practiceType": "MULTIPLE_CHOICE",
  "totalQuestions": 10
}
```

### GET /practice/sessions/:sessionId/questions

Trả danh sách câu hỏi.

### POST /practice/sessions/:sessionId/answers

Lưu câu trả lời.

### POST /practice/sessions/:sessionId/finish

Kết thúc session và tính điểm.

---

## 6.8 Analytics APIs

### GET /analytics/dashboard

Dashboard tổng.

### GET /analytics/decks/:deckId/progress

Progress theo deck.

### GET /analytics/daily-activity

Activity theo khoảng ngày.

---

## 7. Data Model Overview

### 7.1 Main entities

- User
- LearningPath
- LearningLevel
- Deck
- Vocabulary
- ReviewCard
- ReviewLog
- PracticeSession
- PracticeAnswer
- DailyActivity
- NotificationSetting
- DeviceToken
- ImportJob

### 7.2 Relationships

```text
User 1-n Deck
LearningPath 1-n LearningLevel
LearningLevel 1-n Deck
Deck 1-n Vocabulary
Vocabulary 1-n Vocabulary through source_vocabulary_id, self-reference for copied/favorite words

User 1-n ReviewCard
Vocabulary 1-n ReviewCard

User 1-n ReviewLog
ReviewCard 1-n ReviewLog
Vocabulary 1-n ReviewLog

User 1-n PracticeSession
Deck 1-n PracticeSession
PracticeSession 1-n PracticeAnswer
Vocabulary 1-n PracticeAnswer

User 1-1 NotificationSetting
User 1-n DeviceToken
User 1-n ImportJob
Deck 1-n ImportJob
```

### 7.3 Important modeling decisions

#### Favorites

Favorites is not a separate table.

Favorites is a default user deck:

```text
deck_type = USER
is_default = true
name = Favorites
```

When a user favorites a vocabulary, the backend copies that vocabulary into the Favorites deck and sets:

```text
source_vocabulary_id = original vocabulary id
```

#### Duplicate vocabulary

Duplicate exact vocabulary in the same deck is defined by:

```text
deck_id + normalized_word + normalized_meaning
```

Same word with different meaning is allowed.

#### Review card

Review card is based on:

```text
user_id + vocabulary_id
```

not:

```text
user_id + deck_id + vocabulary_id
```

Deck review is implemented by filtering vocabulary.deck_id.

---

## 8. Non-functional Requirements

### 8.1 Performance

- API response thông thường dưới 2 giây.
- Deck/vocabulary list có pagination.
- Import CSV 5k rows phải xử lý được bằng batch validation và batch insert.
- Không query DB từng row khi import.
- Dashboard query cần index tốt.
- Mobile app cần loading state, empty state, error state.

### 8.2 Security

- Password hash bằng bcrypt.
- JWT authentication.
- Không lưu password plain text.
- Validate toàn bộ input.
- User chỉ được sửa/xóa dữ liệu của chính mình.
- User thường không được sửa/xóa system deck.
- User thường không được thêm/sửa/xóa vocabulary thuộc system deck.
- Favorites deck không được xóa.
- CORS config rõ ràng.
- Rate limit login/register, future.

### 8.3 Usability

- UI đơn giản, dễ hiểu.
- Learning flow ít bước.
- Flashcard thao tác nhanh.
- Hiển thị rõ từ cần học và từ cần ôn.
- Deck detail có nút Learn, Review, Practice.
- Add word thủ công có popup xử lý same word different meaning.
- Import CSV trả report rõ ràng.
- Favorite bằng nút tim hoạt động nhanh.

### 8.4 Maintainability

- Backend chia module rõ.
- DTO validation đầy đủ.
- Service tách khỏi controller.
- Prisma migration quản lý schema.
- API response format thống nhất.
- Import logic tách thành service riêng.
- SM-2 logic tách thành service riêng.

---

## 9. SM-2 Algorithm Specification

### 9.1 Rating mapping

```text
AGAIN = 0
HARD  = 3
GOOD  = 4
EASY  = 5
```

### 9.2 ReviewCard fields

- repetition
- intervalDays
- easeFactor
- dueAt
- lastReviewedAt

Default:

```text
repetition = 0
intervalDays = 0
easeFactor = 2.5
dueAt = now
```

### 9.3 Basic algorithm

```text
quality = ratingToQuality(rating)

if quality < 3:
    repetition = 0
    intervalDays = 1
else:
    if repetition == 0:
        intervalDays = 1
    else if repetition == 1:
        intervalDays = 6
    else:
        intervalDays = round(previousIntervalDays * easeFactor)

    repetition = repetition + 1

easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))

if rating == HARD:
    intervalDays = max(1, round(previousIntervalDays * 1.2))

if rating == EASY:
    intervalDays = round(intervalDays * 1.3)

easeFactor = max(1.3, easeFactor)
dueAt = reviewedAt + intervalDays days
```

---

## 10. Audio Strategy

### 10.1 MVP

Sử dụng Android TextToSpeech.

Lợi ích:

- Dễ làm.
- Không cần backend lưu audio.
- Không cần quản lý mp3.
- Có thể dùng cho listening practice.

### 10.2 Future

Có thể thêm:

- audioUrl trong vocabulary.
- Dictionary API để lấy audio.
- Cache audio local.
- Cho phép chọn accent US/UK.

---

## 11. MVP Acceptance Criteria

MVP hoàn thành khi:

1. User đăng ký và đăng nhập được.
2. Khi register, hệ thống tự tạo Favorites deck.
3. User chọn TOEIC/IELTS và level được.
4. App hiển thị deck hệ thống theo level.
5. User xem danh sách từ trong deck.
6. User tạo deck cá nhân rỗng được.
7. User thêm từ thủ công vào deck cá nhân được.
8. Thêm thủ công có check duplicate exact.
9. Thêm thủ công có popup khi same word different meaning.
10. User import CSV vào deck cá nhân thường được.
11. Import CSV skip duplicate và trả report.
12. User bấm tim để copy từ vào Favorites được.
13. Favorites check trùng theo `source_vocabulary_id`.
14. User học flashcard được.
15. User chọn Again/Hard/Good/Easy sau mỗi flashcard.
16. Backend tính SM-2 và lưu review card/log.
17. User ôn tập global được.
18. User ôn tập theo deck được.
19. User luyện trắc nghiệm được.
20. User luyện điền từ được.
21. Dashboard hiển thị số từ đã học, từ cần ôn, streak, accuracy.
22. Android phát âm bằng TextToSpeech được.

---

## 12. Suggested Development Phases

### Phase 1: Backend foundation

- Setup NestJS.
- Setup Prisma + MySQL.
- Create database schema.
- Auth register/login.
- JWT guard.
- User profile.
- Register side effects: notification settings + Favorites deck.
- Seed learning paths and levels.

### Phase 2: Content management

- Deck APIs.
- Vocabulary APIs.
- Duplicate validation.
- Manual add popup flow support.
- Favorite copy flow.
- Seed system decks.
- Import CSV.

### Phase 3: Android foundation

- Setup Android Compose project.
- Login/Register screens.
- Token storage.
- Home screen.
- Path/level selection.
- Deck list.
- Deck detail.
- Vocabulary list.

### Phase 4: Learning engine

- ReviewCard model.
- ReviewLog model.
- SM-2 service.
- Global due review API.
- Deck due review API.
- Flashcard UI.
- TextToSpeech.

### Phase 5: Practice and progress

- Multiple choice.
- Fill in blank.
- Practice session.
- Dashboard.
- Deck progress.
- Daily activity.
- Streak.

### Phase 6: Polish

- Notification local.
- Better UI/UX.
- Loading/error/empty states.
- CSV export.
- Room cache, optional.
- Google login, optional.

---

## 13. Backend Module Structure

```text
src/
├── app.module.ts
├── main.ts
├── auth/
├── users/
├── learning-paths/
├── levels/
├── decks/
├── vocabularies/
├── imports/
├── learning/
├── practice/
├── analytics/
├── notifications/
├── prisma/
└── common/
```

### Important backend services

- `AuthService`
- `DeckService`
- `VocabularyService`
- `FavoriteService`, can be part of VocabularyService
- `ImportService`
- `LearningService`
- `Sm2Service`
- `PracticeService`
- `AnalyticsService`

---

## 14. Important Rules for Agents

1. Favorites is a default user deck, not a separate favorite table.
2. Do not create `user_favorite_vocabularies`.
3. When registering a user, create Favorites deck automatically.
4. Favorites deck is only updated by heart button.
5. Favorites deck cannot be imported into directly.
6. Favorites deck cannot be deleted.
7. When favorite is clicked, copy vocabulary into Favorites and set `source_vocabulary_id`.
8. Check duplicate in Favorites by `source_vocabulary_id`.
9. Manual add duplicate exact is `deck_id + normalized_word + normalized_meaning`.
10. Manual add same word with different meaning should trigger popup flow.
11. Import CSV should skip duplicates and return counts.
12. Import should not fail the whole file because some rows duplicate.
13. Same word with different meaning is allowed.
14. ReviewCard must be based on `user_id + vocabulary_id`.
15. Do not add `deck_id` to review_cards for SRS state.
16. Deck review is implemented by filtering `vocabularies.deck_id`.
17. Backend computes SM-2 in MVP.
18. Android uses TextToSpeech for audio in MVP.
