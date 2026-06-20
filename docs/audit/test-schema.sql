CREATE TABLE students (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INTEGER,
    major VARCHAR(50),
    gpa NUMERIC(3,2)
);

CREATE TABLE courses (
    course_id INTEGER PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    credits INTEGER
);

CREATE TABLE enrollments (
    student_id INTEGER REFERENCES students(id),
    course_id INTEGER REFERENCES courses(course_id),
    grade NUMERIC(3,2),
    PRIMARY KEY (student_id, course_id)
);

INSERT INTO students (id, name, age, major, gpa) VALUES
    (1, 'Alice', 22, 'CS', 3.8),
    (2, 'Bob', 24, 'Math', 3.2),
    (3, 'Carol', 21, 'CS', 3.9),
    (4, 'Dave', 23, 'Physics', 2.9);

INSERT INTO courses (course_id, title, credits) VALUES
    (10, 'Databases', 6),
    (20, 'Algorithms', 6),
    (30, 'Calculus', 9);

INSERT INTO enrollments (student_id, course_id, grade) VALUES
    (1, 10, 1.3),
    (1, 20, 1.7),
    (2, 30, 2.3),
    (3, 10, 1.0);
