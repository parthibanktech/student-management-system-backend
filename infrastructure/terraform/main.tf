provider "aws" {
  region = var.aws_region
}

resource "aws_security_group" "sms_sg" {
  name        = "sms-security-group"
  description = "Allow Web and SSH traffic"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # In real prod, restrict to your VPN IP
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 8761
    to_port     = 8761
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "sms_server" {
  ami           = "ami-0c7217cdde317cfec" # Ubuntu 22.04 LTS (us-east-1) - Change for your region
  instance_type = "t2.medium"             # Minimum for Java microservices
  key_name      = var.key_name
  security_groups = [aws_security_group.sms_sg.name]

  user_data = file("${path.module}/../scripts/user_data.sh")

  tags = {
    Name = "StudentManagementSystem-Server"
  }
}

output "public_ip" {
  value = aws_instance.sms_server.public_ip
}
