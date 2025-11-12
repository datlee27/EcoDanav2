-- Create BankAccount table
CREATE TABLE BankAccount (
    BankAccountId VARCHAR(36) PRIMARY KEY,
    UserId VARCHAR(36) NOT NULL,
    AccountNumber VARCHAR(50) NOT NULL,
    AccountHolderName VARCHAR(100) NOT NULL,
    BankName VARCHAR(100) NOT NULL,
    BankCode VARCHAR(20),
    QRCodeImagePath VARCHAR(500),
    IsDefault BOOLEAN NOT NULL DEFAULT FALSE,
    CreatedDate DATETIME NOT NULL,
    UpdatedDate DATETIME,
    FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
);

-- Create RefundRequest table
CREATE TABLE RefundRequest (
    RefundRequestId VARCHAR(36) PRIMARY KEY,
    BookingId VARCHAR(36) NOT NULL,
    UserId VARCHAR(36) NOT NULL,
    BankAccountId VARCHAR(36) NOT NULL,
    RefundAmount DECIMAL(10,2) NOT NULL,
    CancelReason TEXT NOT NULL,
    Status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    AdminNotes TEXT,
    ProcessedBy VARCHAR(36),
    CreatedDate DATETIME NOT NULL,
    ProcessedDate DATETIME,
    IsWithinTwoHours BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (BookingId) REFERENCES Booking(BookingId),
    FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE,
    FOREIGN KEY (BankAccountId) REFERENCES BankAccount(BankAccountId)
);

-- Create indexes for better performance
CREATE INDEX idx_bankaccount_userid ON BankAccount(UserId);
CREATE INDEX idx_bankaccount_isdefault ON BankAccount(IsDefault);
CREATE INDEX idx_refundrequest_bookingid ON RefundRequest(BookingId);
CREATE INDEX idx_refundrequest_userid ON RefundRequest(UserId);
CREATE INDEX idx_refundrequest_status ON RefundRequest(Status);
CREATE INDEX idx_refundrequest_createddate ON RefundRequest(CreatedDate);
