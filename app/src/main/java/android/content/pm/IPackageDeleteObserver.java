/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\WorkSpace_Xiexin\\Android\\Src\\Demo\\app\\src\\main\\aidl\\android\\content\\pm\\IPackageDeleteObserver.aidl
 */
package android.content.pm;

/**
 * API for deletion callbacks from the Package Manager.
 * <p>
 * {@hide}
 */
public interface IPackageDeleteObserver extends android.os.IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements IPackageDeleteObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver";

        /** Construct the stub at attach it to the interface. */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IPackageDeleteObserver interface,
         * generating a proxy if needed.
         */
        public static IPackageDeleteObserver asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IPackageDeleteObserver))) {
                return ((IPackageDeleteObserver) iin);
            }
            return new Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_packageDeleted: {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0;
                    _arg0 = data.readString();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.packageDeleted(_arg0, _arg1);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IPackageDeleteObserver {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void packageDeleted(String packageName, int returnCode) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(returnCode);
                    mRemote.transact(Stub.TRANSACTION_packageDeleted, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_packageDeleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void packageDeleted(String packageName, int returnCode) throws android.os.RemoteException;
}
