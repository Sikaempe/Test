pipeline {
    agent any

    environment {
        BITBUCKET_URL = 'http://bitbucket:7990/scm/sync/syncme.git'
        GITHUB_REPO_URL = 'https://github.com/Sikaempe/test-repo.git'
    }

    stages {
        stage('Cleanup Workspace') {
            steps {
                script {
                    echo "Cleaning up the workspace to avoid directory conflicts..."
                    deleteDir() // This will delete the contents of the workspace
                }
            }
        }

        stage('Clone Bitbucket Repository') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'bitbucket-token', usernameVariable: 'BITBUCKET_USERNAME', passwordVariable: 'BITBUCKET_TOKEN')]) {
                    script {
                        echo "Cloning Bitbucket repository..."
                        sh """
                            git clone https://$BITBUCKET_USERNAME:$BITBUCKET_TOKEN@$BITBUCKET_URL
                        """
                    }
                }
            }
        }

        stage('Add GitHub Remote') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
                    script {
                        echo "Adding GitHub remote..."
                        sh """
                            cd syncme
                            git remote add github https://$GITHUB_USERNAME:$GITHUB_TOKEN@$GITHUB_REPO_URL
                        """
                    }
                }
            }
        }

        stage('Push All Branches to GitHub') {
            steps {
                script {
                    echo "Pushing all branches to GitHub..."
                    sh """
                        cd syncme
                        git push github --all
                    """
                }
            }
        }

        stage('Push All Tags to GitHub') {
            steps {
                script {
                    echo "Pushing all tags to GitHub..."
                    sh """
                        cd syncme
                        git push github --tags
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Bitbucket repository successfully synced to GitHub."
        }
        failure {
            echo "Failed to sync Bitbucket repository to GitHub."
        }
    }
}
