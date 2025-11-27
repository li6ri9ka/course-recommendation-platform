#!/bin/bash

# ===========================================
# API Testing Script for Course Recommendation Platform
# ===========================================

BASE_URL_USER="http://localhost:8081"
BASE_URL_COURSE="http://localhost:8082"
BASE_URL_RATING="http://localhost:8083"
BASE_URL_RECOMMENDATION="http://localhost:8084"

echo "=========================================="
echo "Testing Course Recommendation Platform"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# Wait for services to be ready
echo -e "\n${YELLOW}Waiting for services to be ready...${NC}"
sleep 5

# ===========================================
# 1. Test UserService
# ===========================================
echo -e "\n${YELLOW}=== Testing UserService ===${NC}"

# Register user 1
echo -e "\n1. Registering user 1..."
RESPONSE=$(curl -s -X POST "$BASE_URL_USER/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "email": "test1@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User1"
  }')
echo "Response: $RESPONSE"
TOKEN1=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
USER_ID1=$(echo $RESPONSE | grep -o '"userId":[0-9]*' | cut -d':' -f2)
print_result $? "Register user 1"

# Register user 2
echo -e "\n2. Registering user 2..."
RESPONSE=$(curl -s -X POST "$BASE_URL_USER/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "test2@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User2"
  }')
echo "Response: $RESPONSE"
TOKEN2=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
USER_ID2=$(echo $RESPONSE | grep -o '"userId":[0-9]*' | cut -d':' -f2)
print_result $? "Register user 2"

# Login
echo -e "\n3. Testing login..."
RESPONSE=$(curl -s -X POST "$BASE_URL_USER/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "password123"
  }')
echo "Response: $RESPONSE"
print_result $? "Login"

# Get user
echo -e "\n4. Getting user by ID..."
RESPONSE=$(curl -s -X GET "$BASE_URL_USER/api/users/$USER_ID1" \
  -H "Authorization: Bearer $TOKEN1")
echo "Response: $RESPONSE"
print_result $? "Get user by ID"

# ===========================================
# 2. Test CourseService
# ===========================================
echo -e "\n${YELLOW}=== Testing CourseService ===${NC}"

# Create courses
echo -e "\n5. Creating course 1 (Java Basics)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_COURSE/api/courses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{
    "title": "Java Basics",
    "description": "Learn Java from scratch",
    "category": "Programming",
    "instructorName": "John Doe",
    "price": 2999.00,
    "durationHours": 20,
    "level": "BEGINNER",
    "published": true
  }')
echo "Response: $RESPONSE"
COURSE_ID1=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
print_result $? "Create course 1"

echo -e "\n6. Creating course 2 (Spring Boot)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_COURSE/api/courses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{
    "title": "Spring Boot Masterclass",
    "description": "Complete Spring Boot course",
    "category": "Programming",
    "instructorName": "Jane Smith",
    "price": 4999.00,
    "durationHours": 40,
    "level": "INTERMEDIATE",
    "published": true
  }')
echo "Response: $RESPONSE"
COURSE_ID2=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
print_result $? "Create course 2"

echo -e "\n7. Creating course 3 (Kotlin)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_COURSE/api/courses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{
    "title": "Kotlin for Android",
    "description": "Build Android apps with Kotlin",
    "category": "Mobile Development",
    "instructorName": "Mike Johnson",
    "price": 3999.00,
    "durationHours": 30,
    "level": "INTERMEDIATE",
    "published": true
  }')
echo "Response: $RESPONSE"
COURSE_ID3=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
print_result $? "Create course 3"

echo -e "\n8. Creating course 4 (Python)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_COURSE/api/courses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d '{
    "title": "Python Machine Learning",
    "description": "ML with Python and scikit-learn",
    "category": "Data Science",
    "instructorName": "Alice Brown",
    "price": 5999.00,
    "durationHours": 50,
    "level": "ADVANCED",
    "published": true
  }')
echo "Response: $RESPONSE"
COURSE_ID4=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
print_result $? "Create course 4"

# Get all courses
echo -e "\n9. Getting all published courses..."
RESPONSE=$(curl -s -X GET "$BASE_URL_COURSE/api/courses?published=true")
echo "Response: $RESPONSE"
print_result $? "Get all courses"

# Search courses
echo -e "\n10. Searching courses by keyword 'Java'..."
RESPONSE=$(curl -s -X GET "$BASE_URL_COURSE/api/courses/search?keyword=Java")
echo "Response: $RESPONSE"
print_result $? "Search courses"

# Get categories
echo -e "\n11. Getting all categories..."
RESPONSE=$(curl -s -X GET "$BASE_URL_COURSE/api/courses/categories")
echo "Response: $RESPONSE"
print_result $? "Get categories"

# ===========================================
# 3. Test RatingService
# ===========================================
echo -e "\n${YELLOW}=== Testing RatingService ===${NC}"

# User 1 rates courses
echo -e "\n12. User 1 rates course 1 (5 stars)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RATING/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{
    \"courseId\": $COURSE_ID1,
    \"rating\": 5,
    \"review\": \"Excellent course for beginners!\"
  }")
echo "Response: $RESPONSE"
print_result $? "User 1 rates course 1"

echo -e "\n13. User 1 rates course 2 (4 stars)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RATING/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{
    \"courseId\": $COURSE_ID2,
    \"rating\": 4,
    \"review\": \"Very comprehensive course\"
  }")
echo "Response: $RESPONSE"
print_result $? "User 1 rates course 2"

# User 2 rates courses
echo -e "\n14. User 2 rates course 1 (4 stars)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RATING/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{
    \"courseId\": $COURSE_ID1,
    \"rating\": 4,
    \"review\": \"Good for learning basics\"
  }")
echo "Response: $RESPONSE"
print_result $? "User 2 rates course 1"

echo -e "\n15. User 2 rates course 2 (5 stars)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RATING/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{
    \"courseId\": $COURSE_ID2,
    \"rating\": 5,
    \"review\": \"Best Spring Boot course!\"
  }")
echo "Response: $RESPONSE"
print_result $? "User 2 rates course 2"

echo -e "\n16. User 2 rates course 3 (5 stars)..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RATING/api/ratings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN2" \
  -d "{
    \"courseId\": $COURSE_ID3,
    \"rating\": 5,
    \"review\": \"Amazing Kotlin course!\"
  }")
echo "Response: $RESPONSE"
print_result $? "User 2 rates course 3"

# Get course ratings
echo -e "\n17. Getting ratings for course 1..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RATING/api/ratings/course/$COURSE_ID1")
echo "Response: $RESPONSE"
print_result $? "Get course ratings"

# Get course stats
echo -e "\n18. Getting rating stats for course 1..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RATING/api/ratings/stats/$COURSE_ID1")
echo "Response: $RESPONSE"
print_result $? "Get rating stats"

# Get my ratings
echo -e "\n19. Getting user 1's ratings..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RATING/api/ratings/my" \
  -H "Authorization: Bearer $TOKEN1")
echo "Response: $RESPONSE"
print_result $? "Get my ratings"

# ===========================================
# 4. Test RecommendationService
# ===========================================
echo -e "\n${YELLOW}=== Testing RecommendationService ===${NC}"

# Wait for Kafka events to be processed
echo -e "\n${YELLOW}Waiting for Kafka events to be processed...${NC}"
sleep 5

# Health check
echo -e "\n20. Health check..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RECOMMENDATION/health")
echo "Response: $RESPONSE"
print_result $? "Health check"

# Get user ratings from recommendation service
echo -e "\n21. Getting user 1's ratings from recommendation service..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RECOMMENDATION/api/recommendations/$USER_ID1/ratings")
echo "Response: $RESPONSE"
print_result $? "Get user ratings"

# Trigger recalculation
echo -e "\n22. Triggering recommendation recalculation for user 1..."
RESPONSE=$(curl -s -X POST "$BASE_URL_RECOMMENDATION/api/recommendations/$USER_ID1/recalculate")
echo "Response: $RESPONSE"
print_result $? "Recalculate recommendations"

# Wait for recalculation
sleep 2

# Get recommendations
echo -e "\n23. Getting recommendations for user 1..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RECOMMENDATION/api/recommendations/$USER_ID1")
echo "Response: $RESPONSE"
print_result $? "Get recommendations for user 1"

echo -e "\n24. Getting recommendations for user 2..."
RESPONSE=$(curl -s -X GET "$BASE_URL_RECOMMENDATION/api/recommendations/$USER_ID2")
echo "Response: $RESPONSE"
print_result $? "Get recommendations for user 2"

# ===========================================
# Summary
# ===========================================
echo -e "\n${YELLOW}=========================================="
echo "Testing Complete!"
echo "==========================================${NC}"

echo -e "\n${GREEN}Service URLs:${NC}"
echo "  UserService:          $BASE_URL_USER"
echo "  CourseService:        $BASE_URL_COURSE"
echo "  RatingService:        $BASE_URL_RATING"
echo "  RecommendationService: $BASE_URL_RECOMMENDATION"

echo -e "\n${GREEN}Test Tokens:${NC}"
echo "  User 1 Token: $TOKEN1"
echo "  User 2 Token: $TOKEN2"

echo -e "\n${GREEN}Created Resources:${NC}"
echo "  User 1 ID: $USER_ID1"
echo "  User 2 ID: $USER_ID2"
echo "  Course IDs: $COURSE_ID1, $COURSE_ID2, $COURSE_ID3, $COURSE_ID4"
