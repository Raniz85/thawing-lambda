use std::env;

use aws_sdk_dynamodb::Client;
use aws_sdk_dynamodb::model::AttributeValue;
use lambda_runtime::{service_fn, LambdaEvent, Error};

use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Deserialize)]
struct CreateUser {
    name: String,
}

#[derive(Serialize)]
struct User {
    id: Uuid,
    name: String,
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let func = service_fn(func);
    lambda_runtime::run(func).await?;
    Ok(())
}

async fn func(event: LambdaEvent<CreateUser>) -> Result<User, Error> {
    let (user, _context) = event.into_parts();
    let config = aws_config::load_from_env().await;
    let user = User {
        id: Uuid::new_v4(),
        name: user.name
    };
    let client = Client::new(&config);
    client.put_item()
        .table_name(env::var("USER_TABLE_NAME").unwrap())
        .item("id", AttributeValue::S(user.id.to_string()))
        .item("name", AttributeValue::S(user.name.clone()))
        .send().await?;
    Ok(user)
}
