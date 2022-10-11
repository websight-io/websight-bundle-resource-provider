package org.apache.sling.bundleresource.impl.reporting;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resource resolver stub used internally to collect resources provided by the bundles.
 */
class ResourceResolverStub implements ResourceResolver {

    @Override
    public @NotNull Resource resolve(@NotNull HttpServletRequest httpServletRequest, @NotNull String s) {
        return null;
    }

    @Override
    public @NotNull Resource resolve(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull Resource resolve(@NotNull HttpServletRequest httpServletRequest) {
        return null;
    }

    @Override
    public @NotNull String map(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull String map(@NotNull HttpServletRequest httpServletRequest, @NotNull String s) {
        return null;
    }

    @Override
    public @Nullable Resource getResource(@NotNull String s) {
        return null;
    }

    @Override
    public @Nullable Resource getResource(Resource resource, @NotNull String s) {
        return null;
    }

    @Override
    public @NotNull String[] getSearchPath() {
        return new String[0];
    }

    @Override
    public @NotNull Iterator<Resource> listChildren(@NotNull Resource resource) {
        return null;
    }

    @Override
    public @Nullable Resource getParent(@NotNull Resource resource) {
        return null;
    }

    @Override
    public @NotNull Iterable<Resource> getChildren(@NotNull Resource resource) {
        return null;
    }

    @Override
    public @NotNull Iterator<Resource> findResources(@NotNull String s, String s1) {
        return null;
    }

    @Override
    public @NotNull Iterator<Map<String, Object>> queryResources(@NotNull String s, String s1) {
        return null;
    }

    @Override
    public boolean hasChildren(@NotNull Resource resource) {
        return false;
    }

    @Override
    public @NotNull ResourceResolver clone(Map<String, Object> map) throws LoginException {
        return null;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public @Nullable String getUserID() {
        return null;
    }

    @Override
    public @NotNull Iterator<String> getAttributeNames() {
        return null;
    }

    @Override
    public @Nullable Object getAttribute(@NotNull String s) {
        return null;
    }

    @Override
    public void delete(@NotNull Resource resource) throws PersistenceException {

    }

    @Override
    public @NotNull Resource create(@NotNull Resource resource, @NotNull String s, Map<String, Object> map) throws PersistenceException {
        return null;
    }

    @Override
    public boolean orderBefore(@NotNull Resource resource, @NotNull String s, @Nullable String s1) throws UnsupportedOperationException, PersistenceException, IllegalArgumentException {
        return false;
    }

    @Override
    public void revert() {

    }

    @Override
    public void commit() throws PersistenceException {

    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public @Nullable String getParentResourceType(Resource resource) {
        return null;
    }

    @Override
    public @Nullable String getParentResourceType(String s) {
        return null;
    }

    @Override
    public boolean isResourceType(Resource resource, String s) {
        return false;
    }

    @Override
    public void refresh() {

    }

    @Override
    public Resource copy(String s, String s1) throws PersistenceException {
        return null;
    }

    @Override
    public Resource move(String s, String s1) throws PersistenceException {
        return null;
    }

    @Override
    public @NotNull Map<String, Object> getPropertyMap() {
        return null;
    }

    @Override
    public <AdapterType> @Nullable AdapterType adaptTo(@NotNull Class<AdapterType> aClass) {
        return null;
    }
}
