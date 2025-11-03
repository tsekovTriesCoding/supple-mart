export interface ErrorResponse {
  timestamp: string;
  status: number;
  message: string;
  errors?: Record<string, string>;
}

export interface ApiError {
  response?: {
    data?: ErrorResponse;
  };
}
