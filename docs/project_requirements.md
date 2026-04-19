# 📄 PROJECT_REQUIREMENTS.md

## Pflege Learning & Exam PWA

## 1. 📌 Project Overview

This project is a Progressive Web Application (PWA) designed to help:

- Azubis in Pflegeausbildung improve their knowledge
- Pflegefachkräfte refresh their professional knowledge

The application provides:

- Domain-based learning using multiple-choice questions (QCM)
- Immediate feedback during practice
- A progression system based on mastery
- An exam system unlocked after full completion

## 2. 👥 User Roles

### 2.1 Student (User)

A learner can:

- Practice questions by domain
- Receive immediate feedback
- Track progress
- Unlock and take exams
- View exam history

### 2.2 Admin

An admin can:

- Create, update, delete questions
- Import questions via CSV/Excel
- Assign domains and difficulty levels

## 3. 🧠 Learning Structure

The platform contains 10 domains

Each domain contains multiple questions

Each question belongs to:

- One domain
- One difficulty level (1⭐, 2⭐, 3⭐)

## 4. ❓ Question Model

### 4.1 Answer Format

Each question contains:

- Exactly 3 answer options
- 1, 2, or 3 correct answers

### 4.2 Scoring Logic (Strict Mode)

A question is considered correct only if the user's selection exactly matches all correct answers.

Example:

- Correct answers: A + C
- User selects: A → ❌ Incorrect
- User selects: A + B → ❌ Incorrect
- User selects: A + C → ✅ Correct

## 5. 🎯 Practice Mode

### 5.1 Domain-Based Practice

When a learner selects a domain:

- Only not mastered questions are shown

### 5.2 Answer Feedback

After each answer:

- Show if the answer is correct or incorrect
- If incorrect, display only the correct answer
- No explanation is shown (MVP)

### 5.3 Question Repetition

Incorrect questions are not repeated immediately

They reappear only in future sessions

### 5.4 Session Behavior

User can leave anytime

Progress is saved automatically

## 6. 🧩 Mastery System

### 6.1 Definition of Mastery

A question is considered mastered when:

- The learner answers it correctly once

### 6.2 Mastery Reversion

If a learner answers a previously mastered question incorrectly:

- It becomes not mastered again

## 7. 🔄 Answer Randomization

Answer options must be displayed in a random order each session

This prevents memorization of positions

## 8. 📊 Progress Tracking

Each domain shows a progress percentage (%)

Progress = number of mastered questions / total questions in domain

## 9. 🧪 Exam System

### 9.1 Unlock Condition

The exam is unlocked when:

- The learner has mastered all questions across all domains

Once unlocked:

- The exam remains available permanently

### 9.2 Exam Composition

Target distribution:

- 5 questions (1⭐ easy)
- 15 questions (2⭐ medium)
- 10 questions (3⭐ hard)

⚠️ MVP Rule:

If there are not enough questions in a difficulty level:

- The system will generate the exam using available questions

### 9.3 Exam Behavior

- Total questions: 30
- No timer
- No immediate feedback during exam
- Feedback shown only at the end

### 9.4 Passing Criteria

Minimum score to pass: 24/30

### 9.5 Exam Retake

- Unlimited attempts
- No cooldown

### 9.6 Post-Exam Logic

All incorrectly answered exam questions:

- Are marked as not mastered
- Return to practice mode

## 10. 📜 Exam History

Each exam attempt must store:

- Date and time
- Score
- Pass / Fail status
- Number of correct answers
- Number of incorrect answers
- List of incorrectly answered questions

## 11. 🛠️ Admin Features

### 11.1 Question Management (CRUD)

Admin can:

- Create questions
- Edit questions
- Delete questions
- View questions

### 11.2 Question Attributes

Each question must include:

- Domain
- Question text
- 3 answer options
- Correct answer(s)
- Difficulty level:
- 1⭐ Easy
- 2⭐ Medium
- 3⭐ Hard

### 11.3 Bulk Import

Admin can upload questions via:

- CSV
- Excel

## 12. 🔐 Authentication

Users must create an account to use the app

Supported login methods:

- Email + password
- Google login

Anonymous usage is not allowed

## 13. 🌍 Language

Application language: German only

Question content: German only

## 14. 📱 PWA Requirements

The application must:

- Be responsive (mobile, tablet, desktop)
- Be installable as a PWA
- Load quickly
- Save user progress reliably
- Provide secure authentication
- Restrict admin access to authorized users only

## 15. ⚠️ MVP Constraints & Trade-offs

### 15.1 No Explanation System

No explanation for answers in MVP

This may reduce learning depth

### 15.2 Flexible Exam Generation

Exams may have inconsistent difficulty distribution

This may impact fairness between users

## 16. 🚀 Future Improvements (Out of Scope for MVP)

- Answer explanations
- Spaced repetition system
- Gamification (XP, streaks, badges)
- Advanced analytics
- Multi-language support
- Offline mode
- AI-generated explanations

## 17. 🧩 Core Learning Loop

The system follows this cycle:

Practice → Feedback → Mastery → Exam → Mistakes → Back to Practice

## 18. ✅ Definition of Success

The product is successful if:

- Users can complete domains and track progress
- Users unlock and take exams
- Wrong answers are reinforced through repetition
- Admin can easily manage and import questions
