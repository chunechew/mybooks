package co.hanbin.mybooks.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.hanbin.mybooks.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(String userId);
}

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
// import org.springframework.data.querydsl.QuerydslPredicateExecutor;
// import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// import co.hanbin.mybooks.user.entity.User;

// @RepositoryRestResource()
// public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User>, QuerydslPredicateExecutor<User> {
    
// }