package com.nextbar.dropPoint.security;

import java.util.List;

public record RbacClaims(List<String> roles, List<String> assignments) {
}
