"""
Streams tweets using X/Twitter API v2 (Bearer token via Tweepy StreamingClient).
Initializes Kafka producer and ingests the streaming tweets to a topic.
"""
import json

import tweepy
from kafka import KafkaProducer

import twitter_credentials as tc


KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
KAFKA_TOPIC = "twitter-topic"
STREAM_RULES = ["Covid19", "coronavirus", "covid"]


class TweetStreamClient(tweepy.StreamingClient):
    def __init__(self, bearer_token: str, producer: KafkaProducer):
        super().__init__(bearer_token=bearer_token, wait_on_rate_limit=True)
        self.producer = producer

    def on_tweet(self, tweet):
        # tweet_fields includes created_at in filter() call below.
        if not tweet.text:
            return

        payload = {
            "created_at": str(tweet.created_at) if tweet.created_at else "",
            "message": tweet.text.replace(",", ""),
        }
        self.producer.send(
            KAFKA_TOPIC, value=json.dumps(payload).encode("utf-8")
        )

    def on_errors(self, errors):
        print(f"Stream errors: {errors}")

    def on_connection_error(self):
        print("Connection error. Disconnecting stream.")
        self.disconnect()

    def on_exception(self, exception):
        print(f"Stream exception: {exception}")
        return False


def sync_stream_rules(client: tweepy.StreamingClient, rule_values):
    existing = client.get_rules()
    if existing and existing.data:
        client.delete_rules([rule.id for rule in existing.data])

    rules = [tweepy.StreamRule(value=rule) for rule in rule_values]
    client.add_rules(rules)


if __name__ == "__main__":
    if tc.bearer_token in ("", "BEARER_TOKEN"):
        raise ValueError(
            "Set bearer_token in twitter_credentials.py before running."
        )

    kafka_producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS)
    stream_client = TweetStreamClient(tc.bearer_token, kafka_producer)

    sync_stream_rules(stream_client, STREAM_RULES)
    stream_client.filter(tweet_fields=["created_at", "lang"])