-- Initial Schema Migration for xdata-web
CREATE TABLE xdata_course (
    course_id SERIAL PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    instructor_course_id VARCHAR(50) UNIQUE NOT NULL,
    course_year INTEGER,
    course_semester VARCHAR(10),
    course_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_users (
    internal_user_id VARCHAR(255) PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    login_user_id VARCHAR(50) UNIQUE NOT NULL,
    course_id INTEGER REFERENCES xdata_course(course_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO xdata_users (internal_user_id, user_name, email, password, role, login_user_id)
VALUES ('admin-uuid', 'Admin', 'admin@xdata.com', '$2a$10$Y5OZrGjsUicjK3pKtVphO.UghGdrSgREeS7Z6.vRSH.S1I8yS0hS6', 'ADMIN', 'admin1');

CREATE TABLE xdata_db_connections (
    connection_id SERIAL PRIMARY KEY,
    connection_name VARCHAR(255) NOT NULL,
    db_url VARCHAR(255) NOT NULL,
    db_user VARCHAR(100),
    db_password VARCHAR(100),
    course_id INTEGER REFERENCES xdata_course(course_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_schemainfo (
    schema_id SERIAL PRIMARY KEY,
    course_id INTEGER REFERENCES xdata_course(course_id),
    schema_name VARCHAR(255),
    ddltext TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_assignment (
    assignment_id SERIAL PRIMARY KEY,
    assignment_name VARCHAR(255) NOT NULL,
    course_id INTEGER REFERENCES xdata_course(course_id),
    connection_id INTEGER REFERENCES xdata_db_connections(connection_id),
    defaultschemaid INTEGER REFERENCES xdata_schemainfo(schema_id),
    deadline TIMESTAMP,
    soft_deadline TIMESTAMP,
    penalty_percentage FLOAT,
    late_submission_allowed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_queries (
    query_id SERIAL PRIMARY KEY,
    assignment_id INTEGER REFERENCES xdata_assignment(assignment_id),
    query_name VARCHAR(255) NOT NULL,
    query_string TEXT NOT NULL,
    marks FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_student_queries (
    submission_id SERIAL PRIMARY KEY,
    rollnum VARCHAR(50),
    assignment_id INTEGER REFERENCES xdata_assignment(assignment_id),
    query_id INTEGER REFERENCES xdata_queries(query_id),
    querystring TEXT NOT NULL,
    marks FLOAT DEFAULT 0.0,
    verifiedcorrect BOOLEAN DEFAULT FALSE,
    tajudgement BOOLEAN DEFAULT FALSE,
    submissiontime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    student_id VARCHAR(255) REFERENCES xdata_users(internal_user_id),
    evaluation_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_password_reset_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL REFERENCES xdata_users(internal_user_id),
    expiry_date TIMESTAMP NOT NULL
);
CREATE TABLE xdata_audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(255),
    performed_by VARCHAR(255),
    target_user VARCHAR(255),
    details TEXT,
    timestamp TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xdata_lms_credentials (
    id SERIAL PRIMARY KEY,
    lms_name VARCHAR(255),
    client_id VARCHAR(255),
    client_secret VARCHAR(255),
    auth_url VARCHAR(255),
    token_url VARCHAR(255),
    grade_service_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);