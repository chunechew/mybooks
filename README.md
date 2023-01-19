# mybooks
개인용 도서 관리 프로그램. React.js +  Next.js 프론트엔드(BFF 포함)에 Java + Spring Boot + Gradle + JPA + MySQL 백엔드 적용.

## 경로 메모
Next.js 소스는 `src/main/ui/my-app`에 있음.
​
## 설정 방법
### macOS + VScode
#### Java
홈브루 설치하고 터미널에서 아래 실행.
> brew install openjdk@17

VSCode에 Java, Spring Boot, Gradle 관련 확장 기능들 추가.

VSCode에서 설정 들어가서 jdk라고 검색 후 settings.json 편집으로 들어가서 아래 추가.
> "java.jdt.ls.java.home": "/usr/local/opt/openjdk@17",

`src/main` 디렉토리에 `resources-dev` 및 `resources-prod` 디렉토리 생성 후, `src/main/resources/sample_dev_application.yml.txt`을 수정하여 `src/main/resources-dev/application.yml`으로 저장하고, `src/main/resources/sample_prod_application.yml.txt` 을 수정하여 `src/main/resources-prod/application.yml`으로 저장함.

`src/main/resources/sample_dev_application.yml.txt`을 수정하여 `.vscode/launch.json`으로 저장함. 

VSCode 창 닫았다 다시 열고 좌측 실행 및 디버그 버튼 누르고 서버 실행.

#### Next.js
추후 기재