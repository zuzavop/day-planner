<?php

function handle_error($connection) {
  json_response("Query error: " . $connection->error);
}

function run() {
  $config = include __DIR__ . '/db_config.php';
  $connection = new mysqli($config['server'], $config['login'], $config['password'], $config['database']);
  if ($connection->connect_error) {
    json_response("Could not connect to the database");
  }

  $stmt_string = "SELECT MAX(id) FROM calendar";

  $stmt = $connection->prepare($stmt_string) or handle_error($connection);
  $stmt->execute() or handle_error($connection);
  $stmt_result = $stmt->get_result() or handle_error($connection);
  $result = $stmt_result->fetch_assoc();
  $connection->close();

  echo json_encode($result);
}


try {
	run();
}
catch (Exception $e) {
	http_response_code(500);
	header('Content-Type: text/plain');
	echo $e->getMessage();
}