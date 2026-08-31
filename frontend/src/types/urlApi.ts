export type ShortenUrlRequest = {
  fullUrl: string;
  customAlias?: string;
};

export type ShortenUrlResponse = {
  customAlias: string;
  fullUrl: string;
  shortUrl: string;
  createdAt?: string;
};

export type UrlListItem = {
  customAlias: string;
  fullUrl: string;
  shortUrl?: string;
  createdAt?: string;
};
