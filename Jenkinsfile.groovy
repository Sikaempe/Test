pipeline {
    agent { label 'linux&&azure' }

    environment {
        BITBUCKET_URL = 'https://bitbucket.e-konzern.de/scm/btcvpp/test-repo.git'
        GITHUB_REPO_URL = 'https://github.com/Sikaempe/Test-repo.git'
    }

    stages {
        stage('Clone Bitbucket Repository') {
            steps {
                script {
                    echo "Cloning Bitbucket repository..."
                    sh """
                        rm -r ./*
                    """
                    withCredentials([usernamePassword(credentialsId: 'scm-default', passwordVariable: 'BITBUCKET_PASS', usernameVariable: 'BITBUCKET_USER')]) {
                        withCredentials([string(credentialsId: 'github-token-simon', variable: 'GITHUB_TOKEN')]) {
                            sh """
                                if [ ! -d "test-repo" ]; then
                                    git clone https://$BITBUCKET_USER:$BITBUCKET_PASS@bitbucket.e-konzern.de/scm/btcvpp/test-repo.git
                                else
                                    echo "Repository already cloned."
                                fi
                                cd test-repo
                                git fetch origin
                                git pull origin sikaempe-testbranch:sikaempe-testbranch
                                git remote add new-origin https://${GITHUB_TOKEN}@github.com/Sikaempe/Test.git
                                git push --all new-origin
                                git push --tags new-origin
                                git remote -v
                            """
                        }
                    }
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
