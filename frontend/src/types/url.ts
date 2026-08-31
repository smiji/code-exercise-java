export type UrlRequest = {
  fullUrl: string;
  customAlias?: string;
};

export type UrlResponse = {
  customAlias: string;
  fullUrl: string;
  shortUrl: string;
  createdAt?: string;
};

export type UrlItem = {
  customAlias: string;
  fullUrl: string;
  shortUrl?: string;
  createdAt?: string;
};
